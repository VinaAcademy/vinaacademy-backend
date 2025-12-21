package com.vinaacademy.platform.feature.review.service;

import com.vinaacademy.platform.feature.common.exception.ResourceNotFoundException;
import com.vinaacademy.platform.feature.user.UserRepository;
import com.vinaacademy.platform.feature.course.entity.Course;
import com.vinaacademy.platform.feature.course.repository.CourseRepository;
import com.vinaacademy.platform.feature.review.dto.CourseReviewDto;
import com.vinaacademy.platform.feature.review.dto.sentiment.*;
import com.vinaacademy.platform.feature.review.entity.*;
import com.vinaacademy.platform.feature.review.enums.FlagType;
import com.vinaacademy.platform.feature.review.enums.ModerationStatus;
import com.vinaacademy.platform.feature.review.enums.PhraseType;
import com.vinaacademy.platform.feature.review.enums.SentimentType;
import com.vinaacademy.platform.feature.review.mapper.CourseReviewMapper;
import com.vinaacademy.platform.feature.review.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service để truy vấn dữ liệu phân tích cảm xúc
 * Cung cấp dữ liệu cho giao diện Sinh viên, Giảng viên và Admin
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewSentimentQueryService {
    
    private final CourseReviewRepository reviewRepository;
    private final ReviewSentimentAnalysisRepository sentimentRepository;
    private final ReviewKeyPhraseRepository keyPhraseRepository;
    private final ReviewModerationFlagRepository flagRepository;
    private final CourseSentimentStatisticsRepository statisticsRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    
    /**
     * Lấy tóm tắt điểm cộng/điểm trừ cho khóa học (cho sinh viên)
     */
    @Transactional(readOnly = true)
    public ProsConsResponse getProsConsSummary(UUID courseId, int limit) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học: " + courseId));
        
        // Get top pros
        List<Object[]> prosData = keyPhraseRepository.findTopProsByCourseId(courseId);
        List<ProsConsResponse.KeyPhraseItem> pros = prosData.stream()
            .limit(limit)
            .map(row -> ProsConsResponse.KeyPhraseItem.builder()
                .phrase((String) row[0])
                .count(((Number) row[1]).intValue())
                .build())
            .collect(Collectors.toList());
        
        // Get top cons
        List<Object[]> consData = keyPhraseRepository.findTopConsByCourseId(courseId);
        List<ProsConsResponse.KeyPhraseItem> cons = consData.stream()
            .limit(limit)
            .map(row -> ProsConsResponse.KeyPhraseItem.builder()
                .phrase((String) row[0])
                .count(((Number) row[1]).intValue())
                .build())
            .collect(Collectors.toList());
        
        // Get total reviews count
        List<ReviewSentimentAnalysis> allSentiments = sentimentRepository.findByCourseId(courseId);
        
        return ProsConsResponse.builder()
            .courseId(courseId)
            .courseName(course.getName())
            .totalReviews(allSentiments.size())
            .pros(pros)
            .cons(cons)
            .build();
    }
    
    /**
     * Lấy các đánh giá được lọc theo cảm xúc (cho sinh viên)
     */
    @Transactional(readOnly = true)
    public Page<ReviewWithSentimentDto> getReviewsBySentiment(
        UUID courseId,
        SentimentType sentiment,
        Pageable pageable
    ) {
        List<ReviewSentimentAnalysis> sentiments;
        
        if (sentiment != null) {
            sentiments = sentimentRepository.findByCourseIdAndSentiment(courseId, sentiment);
        } else {
            sentiments = sentimentRepository.findByCourseId(courseId);
        }
        
        // Convert to DTOs with pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), sentiments.size());
        
        List<ReviewWithSentimentDto> dtos = sentiments.subList(start, end).stream()
            .map(this::convertToReviewWithSentiment)
            .collect(Collectors.toList());
        
        return new PageImpl<>(dtos, pageable, sentiments.size());
    }
    
    /**
     * Lấy dữ liệu dashboard cho giảng viên
     */
    @Transactional(readOnly = true)
    public SentimentDashboardResponse getInstructorDashboard(
        UUID courseId,
        LocalDate startDate,
        LocalDate endDate
    ) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học: " + courseId));
        
        // Default to last 30 days if no dates provided
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        // Get statistics for the period
        List<CourseSentimentStatistics> stats = statisticsRepository.findByCourseIdAndDateRange(
            courseId,
            CourseSentimentStatistics.PeriodType.DAILY,
            startDate,
            endDate
        );
        
        // Build overview
        SentimentDashboardResponse.OverviewStats overview = buildOverviewStats(courseId, stats);
        
        // Build trend data
        List<SentimentDashboardResponse.TrendDataPoint> trendData = buildTrendData(stats);
        
        // Build aspect analysis
        List<SentimentDashboardResponse.AspectAnalysis> aspectAnalysis = buildAspectAnalysis(stats);
        
        // Get recent improvements and concerns
        List<String> recentImprovements = extractRecentPros(courseId, 5);
        List<String> recentConcerns = extractRecentCons(courseId, 5);
        
        return SentimentDashboardResponse.builder()
            .courseId(courseId)
            .courseName(course.getName())
            .period(SentimentDashboardResponse.PeriodInfo.builder()
                .startDate(startDate)
                .endDate(endDate)
                .periodType("DAILY")
                .build())
            .overview(overview)
            .sentimentTrend(trendData)
            .topAspects(aspectAnalysis)
            .recentImprovements(recentImprovements)
            .recentConcerns(recentConcerns)
            .build();
    }
    
    /**
     * Lấy các đánh giá bị gắn cờ để admin kiểm duyệt
     * Nếu status = null, trả về TẤT CẢ các flags (bao gồm PENDING, APPROVED, REJECTED, AUTO_APPROVED)
     * với PENDING được ưu tiên lên trước
     */
    @Transactional(readOnly = true)
    public Page<FlaggedReviewDto> getFlaggedReviews(ModerationStatus status, Pageable pageable) {
        Page<ReviewModerationFlag> flags;
        
        if (status != null) {
            // Lọc theo status cụ thể
            flags = flagRepository.findByStatus(status, pageable);
        } else {
            // Lấy TẤT CẢ flags, PENDING lên trước, sort theo severity
            flags = flagRepository.findAllFlagsOrderedForQueue(pageable);
        }
        
        return flags.map(this::convertToFlaggedReviewDto);
    }
    
    /**
     * Lấy các đánh giá đã xử lý (APPROVED, REJECTED, AUTO_APPROVED)
     * Dành riêng cho tab Lịch sử kiểm duyệt
     */
    @Transactional(readOnly = true)
    public Page<FlaggedReviewDto> getProcessedFlags(Pageable pageable) {
        Page<ReviewModerationFlag> flags = flagRepository.findProcessedFlags(pageable);
        return flags.map(this::convertToFlaggedReviewDto);
    }
    
    /**
     * Lấy thống kê kiểm duyệt cho dashboard admin
     */
    @Transactional(readOnly = true)
    public ModerationStatisticsDto getModerationStatistics() {
        List<Object[]> stats = flagRepository.getModerationStatistics();
        
        Map<String, Long> statusCounts = new HashMap<>();
        for (Object[] row : stats) {
            ModerationStatus status = (ModerationStatus) row[0];
            Long count = (Long) row[1];
            statusCounts.put(status.name(), count);
        }
        
        long criticalPending = flagRepository.countCriticalPending();
        long totalFlags = statusCounts.values().stream().mapToLong(Long::longValue).sum();
        
        return ModerationStatisticsDto.builder()
            .pending(statusCounts.getOrDefault("PENDING", 0L))
            .reviewed(statusCounts.getOrDefault("REVIEWED", 0L))
            .approved(statusCounts.getOrDefault("APPROVED", 0L))
            .rejected(statusCounts.getOrDefault("REJECTED", 0L))
            .autoApproved(statusCounts.getOrDefault("AUTO_APPROVED", 0L))
            .totalFlags(totalFlags)
            .criticalPending(criticalPending)
            .build();
    }
    
    // ========== Helper Methods ==========
    
    private ReviewWithSentimentDto convertToReviewWithSentiment(ReviewSentimentAnalysis sentiment) {
        CourseReview review = sentiment.getReview();
        CourseReviewDto reviewDto = CourseReviewMapper.INSTANCE.toDto(review);
        
        ReviewSentimentDto sentimentDto = ReviewSentimentDto.builder()
            .id(sentiment.getId())
            .reviewId(review.getId())
            .sentiment(sentiment.getSentiment())
            .scores(SentimentScoresDto.builder()
                .positive(sentiment.getConfidencePositive())
                .neutral(sentiment.getConfidenceNeutral())
                .negative(sentiment.getConfidenceNegative())
                .build())
            .isToxic(sentiment.getIsToxic())
            .toxicityScore(sentiment.getToxicityScore())
            .languageDetected(sentiment.getLanguageDetected())
            .analyzedAt(sentiment.getAnalyzedAt())
            .build();
        
        List<ReviewKeyPhrase> phrases = keyPhraseRepository.findByReviewId(review.getId());
        List<KeyPhraseDto> phraseDtos = phrases.stream()
            .map(this::convertToKeyPhraseDto)
            .collect(Collectors.toList());
        
        boolean isFlagged = flagRepository.existsByReviewIdAndStatus(
            review.getId(),
            ModerationStatus.PENDING
        );
        
        return ReviewWithSentimentDto.builder()
            .review(reviewDto)
            .sentiment(sentimentDto)
            .keyPhrases(phraseDtos)
            .isFlagged(isFlagged)
            .build();
    }
    
    private KeyPhraseDto convertToKeyPhraseDto(ReviewKeyPhrase phrase) {
        return KeyPhraseDto.builder()
            .id(phrase.getId())
            .reviewId(phrase.getReview().getId())
            .phrase(phrase.getPhrase())
            .phraseType(phrase.getPhraseType())
            .sentiment(phrase.getSentiment())
            .confidence(phrase.getConfidence())
            .category(phrase.getCategory())
            .build();
    }
    
    public FlaggedReviewDto convertToFlaggedReviewDto(ReviewModerationFlag flag) {
        CourseReview review = flag.getReview();
        
        // Nếu review đã bị xóa, return null hoặc skip (pending flags shouldn't have deleted reviews)
        if (review == null) {
            log.warn("Flag {} references deleted review, skipping", flag.getId());
            return null;
        }
        
        CourseReviewDto reviewDto = CourseReviewMapper.INSTANCE.toDto(review);
        
        Optional<ReviewSentimentAnalysis> sentiment = sentimentRepository.findByReviewId(review.getId());
        ReviewSentimentDto sentimentDto = sentiment.map(s -> ReviewSentimentDto.builder()
            .id(s.getId())
            .reviewId(review.getId())
            .sentiment(s.getSentiment())
            .scores(SentimentScoresDto.builder()
                .positive(s.getConfidencePositive())
                .neutral(s.getConfidenceNeutral())
                .negative(s.getConfidenceNegative())
                .build())
            .isToxic(s.getIsToxic())
            .toxicityScore(s.getToxicityScore())
            .build()
        ).orElse(null);
        
        // Lấy tên người xử lý nếu có
        String reviewedByName = null;
        if (flag.getReviewedBy() != null) {
            reviewedByName = userRepository.findById(flag.getReviewedBy())
                .map(user -> user.getFullName())
                .orElse(null);
        }
        
        return FlaggedReviewDto.builder()
            .flagId(flag.getId())
            .review(reviewDto)
            .sentiment(sentimentDto)
            .flagType(flag.getFlagType())
            .severity(flag.getSeverity())
            .confidence(flag.getConfidence())
            .reason(flag.getReason())
            .status(flag.getStatus())
            .flaggedAt(flag.getCreatedDate())
            .reviewedBy(flag.getReviewedBy())
            .reviewedByName(reviewedByName)
            .reviewedAt(flag.getReviewedAt())
            .moderatorNotes(flag.getModeratorNotes())
            .build();
    }
    
    private SentimentDashboardResponse.OverviewStats buildOverviewStats(
        UUID courseId,
        List<CourseSentimentStatistics> stats
    ) {
        int totalReviews = stats.stream()
            .mapToInt(CourseSentimentStatistics::getTotalReviews)
            .sum();
        
        Map<SentimentType, Integer> distribution = new HashMap<>();
        distribution.put(SentimentType.POSITIVE, stats.stream().mapToInt(CourseSentimentStatistics::getPositiveCount).sum());
        distribution.put(SentimentType.NEGATIVE, stats.stream().mapToInt(CourseSentimentStatistics::getNegativeCount).sum());
        distribution.put(SentimentType.NEUTRAL, stats.stream().mapToInt(CourseSentimentStatistics::getNeutralCount).sum());
        distribution.put(SentimentType.MIXED, stats.stream().mapToInt(CourseSentimentStatistics::getMixedCount).sum());
        
        Map<SentimentType, Double> percentages = new HashMap<>();
        if (totalReviews > 0) {
            distribution.forEach((type, count) -> 
                percentages.put(type, (count * 100.0) / totalReviews)
            );
        }
        
        List<ReviewSentimentAnalysis> toxicReviews = sentimentRepository.findToxicReviewsByCourseId(courseId);
        
        return SentimentDashboardResponse.OverviewStats.builder()
            .totalReviews(totalReviews)
            .sentimentDistribution(distribution)
            .sentimentPercentages(percentages)
            .overallSentimentScore(calculateOverallScore(distribution, totalReviews))
            .toxicReviewsCount(toxicReviews.size())
            .build();
    }
    
    private List<SentimentDashboardResponse.TrendDataPoint> buildTrendData(
        List<CourseSentimentStatistics> stats
    ) {
        return stats.stream()
            .map(stat -> SentimentDashboardResponse.TrendDataPoint.builder()
                .date(stat.getPeriodStart())
                .positiveCount(stat.getPositiveCount())
                .negativeCount(stat.getNegativeCount())
                .neutralCount(stat.getNeutralCount())
                .mixedCount(stat.getMixedCount())
                .sentimentScore(stat.getOverallSentimentScore())
                .build())
            .collect(Collectors.toList());
    }
    
    private List<SentimentDashboardResponse.AspectAnalysis> buildAspectAnalysis(
        List<CourseSentimentStatistics> stats
    ) {
        // Merge aspect breakdowns from all periods
        Map<String, CourseSentimentStatistics.AspectStats> mergedAspects = new HashMap<>();
        
        for (CourseSentimentStatistics stat : stats) {
            if (stat.getAspectBreakdown() != null) {
                stat.getAspectBreakdown().forEach((category, aspectStats) -> {
                    mergedAspects.merge(category, aspectStats, (existing, newStats) -> 
                        new CourseSentimentStatistics.AspectStats(
                            existing.getMentionCount() + newStats.getMentionCount(),
                            existing.getPositiveCount() + newStats.getPositiveCount(),
                            existing.getNegativeCount() + newStats.getNegativeCount(),
                            (existing.getAvgSentimentScore() + newStats.getAvgSentimentScore()) / 2
                        )
                    );
                });
            }
        }
        
        return mergedAspects.entrySet().stream()
            .sorted((e1, e2) -> e2.getValue().getMentionCount().compareTo(e1.getValue().getMentionCount()))
            .limit(5)
            .map(entry -> SentimentDashboardResponse.AspectAnalysis.builder()
                .category(entry.getKey())
                .totalMentions(entry.getValue().getMentionCount())
                .avgSentimentScore(entry.getValue().getAvgSentimentScore())
                .trend("stable") // Could be enhanced with historical comparison
                .build())
            .collect(Collectors.toList());
    }
    
    private List<String> extractRecentPros(UUID courseId, int limit) {
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        List<ReviewSentimentAnalysis> recentReviews = sentimentRepository
            .findByCourseIdAndDateRange(courseId, since, LocalDateTime.now());
        
        return recentReviews.stream()
            .flatMap(sa -> keyPhraseRepository.findByReviewIdAndPhraseType(
                sa.getReview().getId(), 
                PhraseType.PRO
            ).stream())
            .map(ReviewKeyPhrase::getPhrase)
            .distinct()
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    private List<String> extractRecentCons(UUID courseId, int limit) {
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        List<ReviewSentimentAnalysis> recentReviews = sentimentRepository
            .findByCourseIdAndDateRange(courseId, since, LocalDateTime.now());
        
        return recentReviews.stream()
            .flatMap(sa -> keyPhraseRepository.findByReviewIdAndPhraseType(
                sa.getReview().getId(),
                PhraseType.CON
            ).stream())
            .map(ReviewKeyPhrase::getPhrase)
            .distinct()
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    private Double calculateOverallScore(Map<SentimentType, Integer> distribution, int total) {
        if (total == 0) return 0.0;
        
        int positive = distribution.getOrDefault(SentimentType.POSITIVE, 0);
        int negative = distribution.getOrDefault(SentimentType.NEGATIVE, 0);
        
        return (positive - negative) / (double) total;
    }
}
