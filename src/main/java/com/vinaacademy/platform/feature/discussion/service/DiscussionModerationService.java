package com.vinaacademy.platform.feature.discussion.service;

import com.vinaacademy.platform.exception.NotFoundException;
import com.vinaacademy.platform.feature.course.entity.Course;
import com.vinaacademy.platform.feature.discussion.dto.DiscussionDto;
import com.vinaacademy.platform.feature.discussion.dto.moderation.DiscussionModerationStatisticsDto;
import com.vinaacademy.platform.feature.discussion.dto.moderation.FlaggedDiscussionDto;
import com.vinaacademy.platform.feature.discussion.entity.Discussion;
import com.vinaacademy.platform.feature.discussion.entity.DiscussionModerationFlag;
import com.vinaacademy.platform.feature.discussion.mapper.DiscussionMapper;
import com.vinaacademy.platform.feature.discussion.repository.DiscussionModerationFlagRepository;
import com.vinaacademy.platform.feature.discussion.repository.DiscussionRepository;
import com.vinaacademy.platform.feature.lesson.entity.Lesson;
import com.vinaacademy.platform.feature.review.client.LangAiClient;
import com.vinaacademy.platform.feature.review.dto.langai.LangAiSentimentResponse;
import com.vinaacademy.platform.feature.review.enums.FlagType;
import com.vinaacademy.platform.feature.review.enums.ModerationStatus;
import com.vinaacademy.platform.kafka.NotificationProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vinaacademy.kafka.event.NotificationCreateEvent;
import vn.vinaacademy.kafka.event.NotificationCreateEvent.NotificationType;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Service for discussion moderation - auto-flagging using LangAI sentiment analysis
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DiscussionModerationService {
    
    private final DiscussionModerationFlagRepository flagRepository;
    private final DiscussionRepository discussionRepository;
    private final NotificationProducer notificationProducer;
    private final LangAiClient langAiClient;
    
    // Thresholds for toxicity detection (same as review feature)
    private static final BigDecimal TOXIC_THRESHOLD = new BigDecimal("0.45");
    private static final BigDecimal HIGH_NEGATIVE_THRESHOLD = new BigDecimal("0.6");
    
    /**
     * Result wrapper for moderation check
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ModerationResult {
        private boolean hasNegativeFlag;
        private DiscussionModerationFlag flag; // null if no flag created
    }
    
    /**
     * Automatically check and flag discussion using LangAI sentiment analysis
     * Called when a new discussion is created
     * Returns ModerationResult with flag info (if any)
     */
    @Async
    @Transactional
    public CompletableFuture<ModerationResult> checkAndFlagDiscussionAsync(Discussion discussion) {
        try {
            log.info("Starting sentiment analysis for discussion {}", discussion.getId());
            
            if (discussion == null || discussion.getComment() == null || discussion.getComment().trim().isEmpty()) {
                return CompletableFuture.completedFuture(new ModerationResult(false, null));
            }
            
            // Check if LangAI service is available
            if (!langAiClient.isServiceAvailable()) {
                log.warn("LangAI Service not available, skipping analysis for discussion {}", discussion.getId());
                return CompletableFuture.completedFuture(new ModerationResult(false, null));
            }
            
            // Call LangAI to analyze sentiment
            LangAiSentimentResponse sentimentResponse = langAiClient.analyzeSentiment(
                discussion.getComment(),
                "vi"
            );
            
            // Check and create flags based on sentiment analysis
            List<DiscussionModerationFlag> flags = createFlagsFromSentiment(discussion, sentimentResponse);
            
            if (!flags.isEmpty()) {
                // Check if discussion already has pending flags
                boolean hasPendingFlag = flagRepository.existsByDiscussionIdAndStatus(
                    discussion.getId(),
                    ModerationStatus.PENDING
                );
                
                if (!hasPendingFlag) {
                    List<DiscussionModerationFlag> savedFlags = flagRepository.saveAll(flags);
                    log.warn("Created {} moderation flags for discussion {}", flags.size(), discussion.getId());
                    
                    // Get the most severe flag
                    DiscussionModerationFlag mostSevereFlag = savedFlags.stream()
                        .max(Comparator.comparing(DiscussionModerationFlag::getSeverity))
                        .orElse(null);
                    
                    // Check if any flag is negative type
                    boolean hasNegativeFlag = savedFlags.stream()
                        .anyMatch(flag -> isNegativeFlag(flag.getFlagType()));
                    
                    return CompletableFuture.completedFuture(new ModerationResult(hasNegativeFlag, mostSevereFlag));
                }
            }
            
            return CompletableFuture.completedFuture(new ModerationResult(false, null));
            
        } catch (Exception e) {
            log.error("Error analyzing discussion {}: {}", discussion.getId(), e.getMessage(), e);
            return CompletableFuture.completedFuture(new ModerationResult(false, null));
        }
    }
    
    /**
     * Synchronous version - checks if discussion should be flagged
     * Returns ModerationResult with flag info
     */
    @Transactional
    public ModerationResult checkAndFlagDiscussion(Discussion discussion) {
        try {
            return checkAndFlagDiscussionAsync(discussion).get();
        } catch (Exception e) {
            log.error("Error in synchronous flag check: {}", e.getMessage());
            return new ModerationResult(false, null);
        }
    }
    
    /**
     * Create moderation flags based on LangAI sentiment analysis (same logic as review)
     */
    private List<DiscussionModerationFlag> createFlagsFromSentiment(
        Discussion discussion, 
        LangAiSentimentResponse sentiment
    ) {
        List<DiscussionModerationFlag> flags = new ArrayList<>();
        String content = discussion.getComment();
        
        // Check for toxicity (high negative sentiment)
        BigDecimal negativeScore = sentiment.getScores().getNegative();
        boolean isToxic = negativeScore.compareTo(TOXIC_THRESHOLD) >= 0;
        
        if (isToxic) {
            DiscussionModerationFlag flag = DiscussionModerationFlag.builder()
                .discussion(discussion)
                .flagType(FlagType.TOXIC)
                .severity(5) // Critical
                .confidence(negativeScore)
                .reason("Nội dung có thể chứa ngôn từ độc hại hoặc không phù hợp")
                .status(ModerationStatus.PENDING)
                .build();
            flags.add(flag);
        }
        
        // Check for extreme negative sentiment
        if ("negative".equalsIgnoreCase(sentiment.getSentiment()) &&
            negativeScore.compareTo(HIGH_NEGATIVE_THRESHOLD) >= 0) {
            DiscussionModerationFlag flag = DiscussionModerationFlag.builder()
                .discussion(discussion)
                .flagType(FlagType.EXTREME_NEGATIVE)
                .severity(7) // Medium-high
                .confidence(negativeScore)
                .reason("Bình luận có mức độ tiêu cực rất cao, cần xem xét")
                .status(ModerationStatus.PENDING)
                .build();
            flags.add(flag);
        }
        
        // Check for spam (repeated characters or words)
        if (isSpamContent(content)) {
            DiscussionModerationFlag flag = DiscussionModerationFlag.builder()
                .discussion(discussion)
                .flagType(FlagType.SPAM)
                .severity(2) // Low-medium
                .confidence(new BigDecimal("0.35"))
                .reason("Phát hiện nội dung spam hoặc lặp lại quá nhiều")
                .status(ModerationStatus.PENDING)
                .build();
            flags.add(flag);
        }
        
        return flags;
    }
    
    /**
     * Check if content is spam (repeated characters, words, or patterns)
     */
    private boolean isSpamContent(String content) {
        if (content == null || content.length() < 10) {
            return false;
        }
        
        // Check for repeated characters (e.g., "aaaaaa", "hahahahaha")
        if (content.matches(".*(.)\\1{5,}.*")) {
            return true;
        }
        
        // Check for repeated words
        String[] words = content.split("\\s+");
        if (words.length > 5) {
            Set<String> uniqueWords = new HashSet<>(Arrays.asList(words));
            double uniqueRatio = (double) uniqueWords.size() / words.length;
            if (uniqueRatio < 0.3) { // Less than 30% unique words
                return true;
            }
        }
        
        // Check for too many special characters (might be spam)
        long specialCharCount = content.chars()
            .filter(c -> !Character.isLetterOrDigit(c) && !Character.isWhitespace(c))
            .count();
        double specialCharRatio = (double) specialCharCount / content.length();
        if (specialCharRatio > 0.4) { // More than 40% special characters
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if flag type is negative (should suppress notifications)
     */
    private boolean isNegativeFlag(FlagType flagType) {
        return flagType == FlagType.TOXIC || 
               flagType == FlagType.EXTREME_NEGATIVE ||
               flagType == FlagType.ABUSIVE;
    }
    
    /**
     * Get all flagged discussions by status
     */
    @Transactional(readOnly = true)
    public Page<FlaggedDiscussionDto> getFlaggedDiscussions(ModerationStatus status, Pageable pageable) {
        Page<DiscussionModerationFlag> flags;
        
        if (status != null) {
            flags = flagRepository.findByStatus(status, pageable);
        } else {
            flags = flagRepository.findPendingOrderedBySeverity(pageable);
        }
        
        return flags.map(this::convertToDto);
    }
    
    /**
     * Get moderation history (processed flags)
     */
    @Transactional(readOnly = true)
    public Page<FlaggedDiscussionDto> getModerationHistory(Pageable pageable) {
        return flagRepository.findProcessedFlags(pageable).map(this::convertToDto);
    }
    
    /**
     * Approve flag - hide the discussion and notify user
     */
    @Transactional
    public void approveFlag(Long flagId, String notes, boolean hideDiscussion) {
        DiscussionModerationFlag flag = flagRepository.findById(flagId)
            .orElseThrow(() -> NotFoundException.message("Flag không tồn tại: " + flagId));
        
        flag.approve(notes);
        flagRepository.save(flag);
        
        Discussion discussion = flag.getDiscussion();
        UUID discussionOwnerId = discussion.getUser().getId();
        
        // If requested, we could add a hidden flag to Discussion entity
        // For now, we just approve the flag
        
        // Send notification to discussion owner
        NotificationCreateEvent notification = NotificationCreateEvent.builder()
            .title("Bình luận của bạn đã bị ẩn do vi phạm")
            .content("Lý do: " + (notes != null ? notes : flag.getReason()))
            .targetUrl(null)
            .userId(discussionOwnerId)
            .type(NotificationType.SYSTEM)
            .build();
        
        notificationProducer.sendNotification(notification);
        
        log.info("Flag {} approved. Discussion {} owner {} notified", 
            flagId, discussion.getId(), discussionOwnerId);
    }
    
    /**
     * Reject flag - discussion is fine
     */
    @Transactional
    public void rejectFlag(Long flagId, String notes) {
        DiscussionModerationFlag flag = flagRepository.findById(flagId)
            .orElseThrow(() -> NotFoundException.message("Flag không tồn tại: " + flagId));
        
        flag.reject(notes);
        flagRepository.save(flag);
        
        log.info("Flag {} rejected. Discussion {} is fine", 
            flagId, flag.getDiscussion().getId());
    }
    
    /**
     * Get moderation statistics
     */
    @Transactional(readOnly = true)
    public DiscussionModerationStatisticsDto getModerationStatistics() {
        Long totalPending = flagRepository.countByStatus(ModerationStatus.PENDING);
        Long totalApproved = flagRepository.countByStatus(ModerationStatus.APPROVED);
        Long totalRejected = flagRepository.countByStatus(ModerationStatus.REJECTED);
        Long criticalPending = flagRepository.countCriticalPendingFlags();
        
        // Count by flag type for pending flags
        Map<FlagType, Long> pendingByType = new HashMap<>();
        for (FlagType type : FlagType.values()) {
            Long count = flagRepository.countByStatusAndFlagType(ModerationStatus.PENDING, type);
            if (count > 0) {
                pendingByType.put(type, count);
            }
        }
        
        // Calculate average processing time
        List<DiscussionModerationFlag> processedFlags = flagRepository
            .findFlagsCreatedAfter(LocalDateTime.now().minusDays(30));
        
        Double avgProcessingHours = processedFlags.stream()
            .filter(f -> f.getReviewedAt() != null && f.getCreatedDate() != null)
            .mapToLong(f -> Duration.between(f.getCreatedDate(), f.getReviewedAt()).toHours())
            .average()
            .orElse(0.0);
        
        return DiscussionModerationStatisticsDto.builder()
            .totalPendingFlags(totalPending)
            .totalApprovedFlags(totalApproved)
            .totalRejectedFlags(totalRejected)
            .criticalPendingFlags(criticalPending)
            .pendingByType(pendingByType)
            .averageProcessingTimeHours(avgProcessingHours)
            .build();
    }
    
    /**
     * Check if discussion has negative pending flag
     */
    @Transactional(readOnly = true)
    public boolean hasNegativePendingFlag(UUID discussionId) {
        return flagRepository.hasNegativePendingFlag(discussionId);
    }
    
    // ========== HELPER METHODS ==========
    
    private FlaggedDiscussionDto convertToDto(DiscussionModerationFlag flag) {
        Discussion discussion = flag.getDiscussion();
        DiscussionDto discussionDto = DiscussionMapper.INSTANCE.toDto(discussion);
        
        // Enrich with user info
        discussionDto.setUserFullName(discussion.getUser().getFullName());
        discussionDto.setAvatarUrl(discussion.getUser().getAvatarUrl());
        
        // Get course and lesson info
        Lesson lesson = discussion.getLesson();
        Course course = lesson != null ? lesson.getSection().getCourse() : null;
        
        return FlaggedDiscussionDto.builder()
            .flagId(flag.getId())
            .discussion(discussionDto)
            .flagType(flag.getFlagType())
            .severity(flag.getSeverity())
            .confidence(flag.getConfidence())
            .reason(flag.getReason())
            .status(flag.getStatus())
            .flaggedAt(flag.getCreatedDate())
            .reviewedAt(flag.getReviewedAt())
            .moderatorNotes(flag.getModeratorNotes())
            .courseName(course != null ? course.getName() : null)
            .lessonTitle(lesson != null ? lesson.getTitle() : null)
            .build();
    }
}
