package com.vinaacademy.platform.feature.review.service;

import com.vinaacademy.platform.feature.course.entity.Course;
import com.vinaacademy.platform.feature.course.repository.CourseRepository;
import com.vinaacademy.platform.feature.review.entity.CourseSentimentStatistics;
import com.vinaacademy.platform.feature.review.entity.ReviewKeyPhrase;
import com.vinaacademy.platform.feature.review.entity.ReviewSentimentAnalysis;
import com.vinaacademy.platform.feature.review.enums.PhraseType;
import com.vinaacademy.platform.feature.review.enums.SentimentType;
import com.vinaacademy.platform.feature.review.repository.CourseSentimentStatisticsRepository;
import com.vinaacademy.platform.feature.review.repository.ReviewKeyPhraseRepository;
import com.vinaacademy.platform.feature.review.repository.ReviewSentimentAnalysisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service quản lý cache thống kê cảm xúc
 * Cập nhật dữ liệu tổng hợp cho phân tích dashboard
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SentimentStatisticsService {
    
    private final CourseSentimentStatisticsRepository statisticsRepository;
    private final ReviewSentimentAnalysisRepository sentimentRepository;
    private final ReviewKeyPhraseRepository keyPhraseRepository;
    private final CourseRepository courseRepository;
    
    /**
     * Cập nhật thống kê cho khóa học (gọi sau khi phân tích đánh giá)
     */
    @Async("sentimentAnalysisExecutor")
    @Transactional
    public void updateCourseStatistics(UUID courseId) {
        try {
            log.debug("Đang cập nhật thống kê cảm xúc cho khóa học {}", courseId);
            
            // Cập nhật thống kê theo ngày
            updateStatisticsForPeriod(courseId, CourseSentimentStatistics.PeriodType.DAILY);
            
            // Cập nhật thống kê theo tuần
            updateStatisticsForPeriod(courseId, CourseSentimentStatistics.PeriodType.WEEKLY);
            
            // Cập nhật thống kê theo tháng
            updateStatisticsForPeriod(courseId, CourseSentimentStatistics.PeriodType.MONTHLY);
            
            log.debug("Đã hoàn tất cập nhật thống kê cảm xúc cho khóa học {}", courseId);
            
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật thống kê cho khóa học {}: {}", courseId, e.getMessage(), e);
        }
    }
    
    /**
     * Cập nhật thống kê cho một loại kỳ cụ thể
     */
    @Transactional
    public void updateStatisticsForPeriod(UUID courseId, CourseSentimentStatistics.PeriodType periodType) {
        LocalDate now = LocalDate.now();
        LocalDate periodStart = calculatePeriodStart(now, periodType);
        LocalDate periodEnd = calculatePeriodEnd(periodStart, periodType);
        
        // Find or create statistics record
        Optional<CourseSentimentStatistics> existingStats = statisticsRepository
            .findByCourseIdAndPeriodTypeAndPeriodStart(courseId, periodType, periodStart);
        
        CourseSentimentStatistics stats;
        if (existingStats.isPresent()) {
            stats = existingStats.get();
        } else {
            Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found: " + courseId));
            
            stats = CourseSentimentStatistics.builder()
                .course(course)
                .periodType(periodType)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .build();
        }
        
        // Calculate statistics
        LocalDateTime startDateTime = periodStart.atStartOfDay();
        LocalDateTime endDateTime = periodEnd.plusDays(1).atStartOfDay();
        
        List<ReviewSentimentAnalysis> reviews = sentimentRepository
            .findByCourseIdAndDateRange(courseId, startDateTime, endDateTime);
        
        if (reviews.isEmpty()) {
            log.debug("Không tìm thấy đánh giá cho khóa học {} trong giai đoạn {} đến {}", 
                courseId, periodStart, periodEnd);
            return;
        }
        
        // Count by sentiment
        Map<SentimentType, Long> sentimentCounts = reviews.stream()
            .collect(Collectors.groupingBy(
                ReviewSentimentAnalysis::getSentiment,
                Collectors.counting()
            ));
        
        stats.setTotalReviews(reviews.size());
        stats.setPositiveCount(sentimentCounts.getOrDefault(SentimentType.POSITIVE, 0L).intValue());
        stats.setNegativeCount(sentimentCounts.getOrDefault(SentimentType.NEGATIVE, 0L).intValue());
        stats.setNeutralCount(sentimentCounts.getOrDefault(SentimentType.NEUTRAL, 0L).intValue());
        stats.setMixedCount(sentimentCounts.getOrDefault(SentimentType.MIXED, 0L).intValue());
        
        // Calculate average scores
        stats.setAvgPositiveScore(calculateAverageScore(reviews, ReviewSentimentAnalysis::getConfidencePositive));
        stats.setAvgNegativeScore(calculateAverageScore(reviews, ReviewSentimentAnalysis::getConfidenceNegative));
        stats.setAvgNeutralScore(calculateAverageScore(reviews, ReviewSentimentAnalysis::getConfidenceNeutral));
        
        // Extract top pros and cons
        List<Long> reviewIds = reviews.stream()
            .map(r -> r.getReview().getId())
            .collect(Collectors.toList());
        
        List<String> topPros = extractTopPhrases(reviewIds, PhraseType.PRO, 10);
        List<String> topCons = extractTopPhrases(reviewIds, PhraseType.CON, 10);
        
        stats.setTopPros(topPros);
        stats.setTopCons(topCons);
        
        // Calculate aspect breakdown
        Map<String, CourseSentimentStatistics.AspectStats> aspectBreakdown = 
            calculateAspectBreakdown(reviewIds);
        stats.setAspectBreakdown(aspectBreakdown);
        
        statisticsRepository.save(stats);
        log.debug("Đã cập nhật thống kê {} cho khóa học {}: {} đánh giá", 
            periodType, courseId, reviews.size());
    }
    
    /**
     * Tính toán ngày bắt đầu kỳ dựa trên loại kỳ
     */
    private LocalDate calculatePeriodStart(LocalDate date, CourseSentimentStatistics.PeriodType periodType) {
        return switch (periodType) {
            case DAILY -> date;
            case WEEKLY -> date.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
            case MONTHLY -> date.with(TemporalAdjusters.firstDayOfMonth());
            case QUARTERLY -> {
                int month = date.getMonthValue();
                int quarterStartMonth = ((month - 1) / 3) * 3 + 1;
                yield date.withMonth(quarterStartMonth).with(TemporalAdjusters.firstDayOfMonth());
            }
            case YEARLY -> date.with(TemporalAdjusters.firstDayOfYear());
        };
    }
    
    /**
     * Tính toán ngày kết thúc kỳ dựa trên loại kỳ
     */
    private LocalDate calculatePeriodEnd(LocalDate periodStart, CourseSentimentStatistics.PeriodType periodType) {
        return switch (periodType) {
            case DAILY -> periodStart;
            case WEEKLY -> periodStart.plusDays(6);
            case MONTHLY -> periodStart.with(TemporalAdjusters.lastDayOfMonth());
            case QUARTERLY -> periodStart.plusMonths(3).minusDays(1);
            case YEARLY -> periodStart.with(TemporalAdjusters.lastDayOfYear());
        };
    }
    
    /**
     * Tính toán điểm trung bình từ các đánh giá
     */
    private BigDecimal calculateAverageScore(
        List<ReviewSentimentAnalysis> reviews,
        java.util.function.Function<ReviewSentimentAnalysis, BigDecimal> scoreExtractor
    ) {
        if (reviews.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal sum = reviews.stream()
            .map(scoreExtractor)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return sum.divide(BigDecimal.valueOf(reviews.size()), 4, RoundingMode.HALF_UP);
    }
    
    /**
     * Trích xuất các cụm từ hàng đầu theo tần suất
     */
    private List<String> extractTopPhrases(List<Long> reviewIds, PhraseType phraseType, int limit) {
        if (reviewIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<ReviewKeyPhrase> phrases = reviewIds.stream()
            .flatMap(reviewId -> keyPhraseRepository.findByReviewIdAndPhraseType(reviewId, phraseType).stream())
            .collect(Collectors.toList());
        
        // Count phrase frequencies
        Map<String, Long> phraseCounts = phrases.stream()
            .collect(Collectors.groupingBy(
                ReviewKeyPhrase::getPhrase,
                Collectors.counting()
            ));
        
        // Sort by count and return top N
        return phraseCounts.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(limit)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
    
    /**
     * Tính toán thống kê phân tích theo khía cạnh
     */
    private Map<String, CourseSentimentStatistics.AspectStats> calculateAspectBreakdown(List<Long> reviewIds) {
        if (reviewIds.isEmpty()) {
            return Collections.emptyMap();
        }
        
        List<ReviewKeyPhrase> phrases = reviewIds.stream()
            .flatMap(reviewId -> keyPhraseRepository.findByReviewId(reviewId).stream())
            .filter(p -> p.getCategory() != null)
            .collect(Collectors.toList());
        
        Map<String, CourseSentimentStatistics.AspectStats> breakdown = new HashMap<>();
        
        // Group by category
        Map<String, List<ReviewKeyPhrase>> byCategory = phrases.stream()
            .collect(Collectors.groupingBy(p -> p.getCategory().name()));
        
        for (Map.Entry<String, List<ReviewKeyPhrase>> entry : byCategory.entrySet()) {
            String category = entry.getKey();
            List<ReviewKeyPhrase> categoryPhrases = entry.getValue();
            
            int totalMentions = categoryPhrases.size();
            int positiveCount = (int) categoryPhrases.stream()
                .filter(p -> p.getSentiment() == SentimentType.POSITIVE)
                .count();
            int negativeCount = (int) categoryPhrases.stream()
                .filter(p -> p.getSentiment() == SentimentType.NEGATIVE)
                .count();
            
            // Calculate average sentiment score (-1 to 1)
            double avgScore = 0.0;
            if (totalMentions > 0) {
                avgScore = (positiveCount - negativeCount) / (double) totalMentions;
            }
            
            CourseSentimentStatistics.AspectStats stats = new CourseSentimentStatistics.AspectStats(
                totalMentions,
                positiveCount,
                negativeCount,
                avgScore
            );
            
            breakdown.put(category, stats);
        }
        
        return breakdown;
    }
    
    /**
     * Cập nhật thống kê hàng loạt cho nhiều khóa học
     */
    @Transactional
    public void batchUpdateStatistics(List<UUID> courseIds) {
        log.info("Đang cập nhật thống kê hàng loạt cho {} khóa học", courseIds.size());
        
        for (UUID courseId : courseIds) {
            try {
                updateCourseStatistics(courseId);
            } catch (Exception e) {
                log.error("Lỗi khi cập nhật thống kê cho khóa học {}", courseId, e);
            }
        }
    }
    
    /**
     * Dọn dẹp thống kê cũ (gọi bởi nhiệm vụ được lên lịch)
     */
    @Transactional
    public void cleanupOldStatistics(int daysToKeep) {
        LocalDate cutoffDate = LocalDate.now().minusDays(daysToKeep);
        statisticsRepository.deleteOldStatistics(cutoffDate);
        log.info("Đã dọn dẹp thống kê cũ hơn {}", cutoffDate);
    }
}
