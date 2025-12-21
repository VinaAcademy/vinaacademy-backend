package com.vinaacademy.platform.feature.review.service;

import com.vinaacademy.platform.feature.review.client.LangAiClient;
import com.vinaacademy.platform.feature.review.dto.langai.LangAiKeyPhrasesResponse;
import com.vinaacademy.platform.feature.review.dto.langai.LangAiSentimentResponse;
import com.vinaacademy.platform.feature.review.entity.*;
import com.vinaacademy.platform.feature.review.enums.*;
import com.vinaacademy.platform.feature.review.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service phân tích cảm xúc đánh giá sử dụng Lang-AI Service
 * Xử lý phân tích cảm xúc bất đồng bộ, trích xuất cụm từ khóa và tự động gắn cờ
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SentimentAnalysisService {
    
    private final LangAiClient langAiClient;
    private final ReviewSentimentAnalysisRepository sentimentRepository;
    private final ReviewKeyPhraseRepository keyPhraseRepository;
    private final ReviewModerationFlagRepository flagRepository;
    private final SentimentStatisticsService statisticsService;
    private final CourseReviewRepository reviewRepository;
    
    // Thresholds for toxicity detection
    private static final BigDecimal TOXIC_THRESHOLD = new BigDecimal("0.85");
    private static final BigDecimal HIGH_NEGATIVE_THRESHOLD = new BigDecimal("0.90");
    
    // Patterns for detecting toxic content (basic, can be extended)
    private static final List<Pattern> TOXIC_PATTERNS = Arrays.asList(
        Pattern.compile("\\b(ngu|đần|óc chó|đồ ngu|khốn nạn)\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\b(fuck|shit|damn|bitch|asshole)\\b", Pattern.CASE_INSENSITIVE)
    );
    
    /**
     * Phân tích đánh giá bất đồng bộ
     * Đây là điểm vào chính được gọi khi đánh giá được tạo/cập nhật
     * 
     * Note: Không dùng @Transactional ở đây vì:
     * - Async method cần transaction riêng độc lập
     * - Tránh vấn đề isolation với transaction cha chưa commit
     * - Mỗi bước bên trong (save sentiment, save phrases) có transaction riêng
     */
    @Async("sentimentAnalysisExecutor")
    public CompletableFuture<Void> analyzeReviewAsync(CourseReview review) {
        try {
            log.info("Đang bắt đầu phân tích cảm xúc cho đánh giá ID: {}", review.getId());
            
            // Kiểm tra review có null không
            if (review == null || review.getId() == null) {
                log.warn("Review null hoặc không có ID, bỏ qua phân tích");
                return CompletableFuture.completedFuture(null);
            }
            
            // Kiểm tra review có bị ẩn không
            if (Boolean.TRUE.equals(review.getIsHidden())) {
                log.warn("Review {} đã bị ẩn, bỏ qua phân tích", review.getId());
                return CompletableFuture.completedFuture(null);
            }
            
            // Kiểm tra xem Lang-AI service có khả dụng không
            if (!langAiClient.isServiceAvailable()) {
                log.warn("Lang-AI Service không khả dụng, bỏ qua phân tích cho đánh giá {}", review.getId());
                return CompletableFuture.completedFuture(null);
            }
            
            // 1. Phân tích cảm xúc
            LangAiSentimentResponse sentimentResponse = langAiClient.analyzeSentiment(
                review.getReview(),
                "vi"
            );
            
            // 2. Lưu kết quả phân tích cảm xúc
            ReviewSentimentAnalysis sentiment = saveSentimentAnalysis(review, sentimentResponse);
            
            // 3. Trích xuất và lưu cụm từ khóa
            LangAiKeyPhrasesResponse phrasesResponse = langAiClient.extractKeyPhrases(
                review.getReview(),
                "vi"
            );
            saveKeyPhrases(review, phrasesResponse, sentiment.getSentiment());
            
            // 4. Tự động gắn cờ nếu cần
            checkAndFlagReview(review, sentiment);
            
            // 5. Cập nhật cache thống kê khóa học
            statisticsService.updateCourseStatistics(review.getCourse().getId());
            
            log.info("Đã hoàn tất phân tích cảm xúc cho đánh giá ID: {}", review.getId());
            
        } catch (Exception e) {
            log.error("Lỗi khi phân tích đánh giá {}: {}", review.getId(), e.getMessage(), e);
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Lưu kết quả phân tích cảm xúc vào database
     * Transaction được quản lý riêng cho từng bước
     */
    @Transactional
    private ReviewSentimentAnalysis saveSentimentAnalysis(
        CourseReview review,
        LangAiSentimentResponse response
    ) {
        // Kiểm tra xem phân tích đã tồn tại chưa
        Optional<ReviewSentimentAnalysis> existing = sentimentRepository.findByReviewId(review.getId());
        
        ReviewSentimentAnalysis sentiment;
        if (existing.isPresent()) {
            sentiment = existing.get();
            log.debug("Cập nhật phân tích cảm xúc hiện có cho đánh giá {}", review.getId());
        } else {
            sentiment = new ReviewSentimentAnalysis();
            sentiment.setReview(review);
        }
        
        // Map sentiment type
        SentimentType sentimentType = mapSentimentType(response.getSentiment());
        sentiment.setSentiment(sentimentType);
        
        // Set confidence scores
        sentiment.setConfidencePositive(response.getScores().getPositive());
        sentiment.setConfidenceNeutral(response.getScores().getNeutral());
        sentiment.setConfidenceNegative(response.getScores().getNegative());
        
        // Check for toxicity
        boolean isToxic = checkToxicity(review.getReview(), response);
        sentiment.setIsToxic(isToxic);
        sentiment.setToxicityScore(calculateToxicityScore(response));
        
        // Set metadata
        sentiment.setLanguageDetected("vi");
        sentiment.setAnalysisVersion("v3.1");
        sentiment.setAnalyzedAt(LocalDateTime.now());
        
        return sentimentRepository.save(sentiment);
    }
    
    /**
     * Trích xuất và lưu cụm từ khóa với phân loại
     * Transaction được quản lý riêng
     */
    @Transactional
    private void saveKeyPhrases(
        CourseReview review,
        LangAiKeyPhrasesResponse response,
        SentimentType overallSentiment
    ) {
        // Xóa cụm từ khóa hiện có cho đánh giá này
        keyPhraseRepository.deleteByReviewId(review.getId());
        
        if (response.getKeyPhrases() == null || response.getKeyPhrases().isEmpty()) {
            log.debug("Không tìm thấy cụm từ khóa cho đánh giá {}", review.getId());
            return;
        }
        
        List<ReviewKeyPhrase> phrases = response.getKeyPhrases().stream()
            .map(phraseText -> classifyAndCreatePhrase(review, phraseText, overallSentiment))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
        if (!phrases.isEmpty()) {
            keyPhraseRepository.saveAll(phrases);
            log.debug("Đã lưu {} cụm từ khóa cho đánh giá {}", phrases.size(), review.getId());
        }
    }
    
    /**
     * Phân loại cụm từ thành pro/con/aspect và phân loại nó
     */
    private ReviewKeyPhrase classifyAndCreatePhrase(
        CourseReview review,
        String phraseText,
        SentimentType overallSentiment
    ) {
        ReviewKeyPhrase phrase = new ReviewKeyPhrase();
        phrase.setReview(review);
        phrase.setPhrase(phraseText);
        phrase.setSentiment(overallSentiment);
        
        // Classify phrase type based on keywords and overall sentiment
        PhraseType phraseType = inferPhraseType(phraseText, overallSentiment);
        phrase.setPhraseType(phraseType);
        
        // Categorize the aspect being discussed
        AspectCategory category = categorizePhrase(phraseText);
        phrase.setCategory(category);
        
        // Set confidence (simplified - could be enhanced with ML)
        phrase.setConfidence(new BigDecimal("0.75"));
        
        return phrase;
    }
    
    /**
     * Suy luận loại cụm từ (PRO/CON/ASPECT) dựa trên từ khóa và cảm xúc
     */
    private PhraseType inferPhraseType(String phrase, SentimentType sentiment) {
        String lowerPhrase = phrase.toLowerCase();
        
        // Positive keywords
        List<String> positiveKeywords = Arrays.asList(
            "tốt", "hay", "rõ ràng", "dễ hiểu", "chất lượng", "xuất sắc",
            "hữu ích", "bổ ích", "thú vị", "tuyệt vời", "hoàn hảo"
        );
        
        // Negative keywords
        List<String> negativeKeywords = Arrays.asList(
            "khó", "khó hiểu", "tệ", "kém", "chậm", "lỗi", "thiếu",
            "không rõ", "không tốt", "chưa tốt", "cần cải thiện"
        );
        
        boolean hasPositive = positiveKeywords.stream().anyMatch(lowerPhrase::contains);
        boolean hasNegative = negativeKeywords.stream().anyMatch(lowerPhrase::contains);
        
        // Classify based on keywords and overall sentiment
        if (hasPositive && sentiment == SentimentType.POSITIVE) {
            return PhraseType.PRO;
        } else if (hasNegative && sentiment == SentimentType.NEGATIVE) {
            return PhraseType.CON;
        } else if (hasPositive || hasNegative) {
            return sentiment == SentimentType.POSITIVE ? PhraseType.PRO : PhraseType.CON;
        } else {
            return PhraseType.ASPECT;
        }
    }
    
    /**
     * Phân loại cụm từ vào các danh mục khía cạnh
     */
    private AspectCategory categorizePhrase(String phrase) {
        String lowerPhrase = phrase.toLowerCase();
        
        // Category keywords mapping
        Map<AspectCategory, List<String>> categoryKeywords = new HashMap<>();
        categoryKeywords.put(AspectCategory.CONTENT, 
            Arrays.asList("nội dung", "bài giảng", "kiến thức", "chương trình", "curriculum"));
        categoryKeywords.put(AspectCategory.INSTRUCTOR, 
            Arrays.asList("giảng viên", "thầy", "cô", "instructor", "teacher", "giáo viên"));
        categoryKeywords.put(AspectCategory.TECHNICAL, 
            Arrays.asList("video", "âm thanh", "chất lượng video", "platform", "website", "app"));
        categoryKeywords.put(AspectCategory.SUPPORT, 
            Arrays.asList("hỗ trợ", "support", "trả lời", "phản hồi", "tư vấn"));
        categoryKeywords.put(AspectCategory.DIFFICULTY, 
            Arrays.asList("khó", "dễ", "phức tạp", "đơn giản", "độ khó"));
        categoryKeywords.put(AspectCategory.PACING, 
            Arrays.asList("tiến độ", "tốc độ", "chậm", "nhanh", "pace", "thời gian"));
        categoryKeywords.put(AspectCategory.MATERIALS, 
            Arrays.asList("tài liệu", "bài tập", "exercise", "material", "slide"));
        categoryKeywords.put(AspectCategory.VALUE, 
            Arrays.asList("giá", "price", "đắt", "rẻ", "phí", "cost", "value"));
        
        // Find matching category
        for (Map.Entry<AspectCategory, List<String>> entry : categoryKeywords.entrySet()) {
            if (entry.getValue().stream().anyMatch(lowerPhrase::contains)) {
                return entry.getKey();
            }
        }
        
        return AspectCategory.GENERAL;
    }
    
    /**
     * Kiểm tra xem đánh giá có nên được gắn cờ để kiểm duyệt không
    /**
     * Tự động gắn cờ kiểm duyệt nếu phát hiện nội dung vi phạm
     * Transaction được quản lý riêng
     */
    @Transactional
    private void checkAndFlagReview(CourseReview review, ReviewSentimentAnalysis sentiment) {
        List<ReviewModerationFlag> flags = new ArrayList<>();
        
        // Kiểm tra tính độc hại
        if (sentiment.getIsToxic()) {
            ReviewModerationFlag flag = ReviewModerationFlag.builder()
                .review(review)
                .flagType(FlagType.TOXIC)
                .severity(5)
                .confidence(sentiment.getToxicityScore())
                .reason("Nội dung có thể chứa ngôn từ độc hại hoặc không phù hợp")
                .status(ModerationStatus.PENDING)
                .build();
            flags.add(flag);
        }
        
        // Kiểm tra cảm xúc cực kỳ tiêu cực
        if (sentiment.getSentiment() == SentimentType.NEGATIVE &&
            sentiment.getConfidenceNegative().compareTo(HIGH_NEGATIVE_THRESHOLD) >= 0) {
            ReviewModerationFlag flag = ReviewModerationFlag.builder()
                .review(review)
                .flagType(FlagType.EXTREME_NEGATIVE)
                .severity(3)
                .confidence(sentiment.getConfidenceNegative())
                .reason("Đánh giá có mức độ tiêu cực rất cao, cần xem xét")
                .status(ModerationStatus.PENDING)
                .build();
            flags.add(flag);
        }
        
        // Kiểm tra xem đánh giá đã có cờ chờ xử lý chưa
        if (!flags.isEmpty()) {
            boolean hasPendingFlag = flagRepository.existsByReviewIdAndStatus(
                review.getId(), 
                ModerationStatus.PENDING
            );
            
            if (!hasPendingFlag) {
                flagRepository.saveAll(flags);
                log.info("Đã tạo {} cờ kiểm duyệt cho đánh giá {}", flags.size(), review.getId());
            }
        }
    }
    
    /**
     * Kiểm tra xem văn bản có chứa nội dung độc hại không
     */
    private boolean checkToxicity(String text, LangAiSentimentResponse response) {
        // Method 1: Pattern matching
        boolean hasPatternMatch = TOXIC_PATTERNS.stream()
            .anyMatch(pattern -> pattern.matcher(text).find());
        
        // Method 2: Very high negative confidence
        boolean hasHighNegative = response.getScores().getNegative()
            .compareTo(TOXIC_THRESHOLD) >= 0;
        
        return hasPatternMatch || hasHighNegative;
    }
    
    /**
     * Tính toán điểm số độc hại
     */
    private BigDecimal calculateToxicityScore(LangAiSentimentResponse response) {
        // Tính toán đơn giản: điểm tiêu cực cao hơn = độc hại cao hơn
        return response.getScores().getNegative();
    }
    
    /**
     * Ánh xạ cảm xúc chuỗi sang enum
     */
    private SentimentType mapSentimentType(String sentiment) {
        return switch (sentiment.toLowerCase()) {
            case "positive" -> SentimentType.POSITIVE;
            case "negative" -> SentimentType.NEGATIVE;
            case "neutral" -> SentimentType.NEUTRAL;
            case "mixed" -> SentimentType.MIXED;
            default -> SentimentType.NEUTRAL;
        };
    }
    
    /**
     * Phân tích lại các đánh giá hiện có (cho xử lý hàng loạt)
     */
    @Transactional
    public void reanalyzeReview(Long reviewId) {
        // Triển khai cho phân tích lại thủ công nếu cần
        log.info("Đã yêu cầu phân tích lại thủ công cho đánh giá {}", reviewId);
    }
}
