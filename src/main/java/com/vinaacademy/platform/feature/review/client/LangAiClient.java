package com.vinaacademy.platform.feature.review.client;

import com.vinaacademy.platform.feature.review.dto.langai.LangAiAnalysisRequest;
import com.vinaacademy.platform.feature.review.dto.langai.LangAiKeyPhrasesResponse;
import com.vinaacademy.platform.feature.review.dto.langai.LangAiSentimentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Client để giao tiếp với Lang-AI Service
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LangAiClient {
    
    private final RestTemplate restTemplate;
    
    @Value("${langai.service.url:http://localhost:8085}")
    private String langAiServiceUrl;
    
    @Value("${langai.service.timeout:30000}")
    private int timeout;
    
    /**
     * Gọi API phân tích cảm xúc
     */
    public LangAiSentimentResponse analyzeSentiment(String text, String language) {
        try {
            String url = langAiServiceUrl + "/api/v1/language/analyze-sentiment";
            
            LangAiAnalysisRequest request = LangAiAnalysisRequest.builder()
                .text(text)
                .language(language != null ? language : "vi")
                .build();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<LangAiAnalysisRequest> entity = new HttpEntity<>(request, headers);
            
            log.debug("Gọi Lang-AI Service để phân tích cảm xúc: {}", url);
            
            ResponseEntity<LangAiSentimentResponse> response = restTemplate.postForEntity(
                url,
                entity,
                LangAiSentimentResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.debug("Phân tích cảm xúc thành công cho văn bản độ dài: {}", text.length());
                return response.getBody();
            } else {
                log.error("Phản hồi không mong đợi từ Lang-AI Service: {}", response.getStatusCode());
                throw new RuntimeException("Không thể phân tích cảm xúc");
            }
            
        } catch (RestClientException e) {
            log.error("Lỗi khi gọi Lang-AI Service để phân tích cảm xúc", e);
            throw new RuntimeException("Không thể kết nối với Lang-AI Service", e);
        }
    }
    
    /**
     * Gọi API trích xuất cụm từ khóa
     */
    public LangAiKeyPhrasesResponse extractKeyPhrases(String text, String language) {
        try {
            String url = langAiServiceUrl + "/api/v1/language/extract-key-phrases";
            
            LangAiAnalysisRequest request = LangAiAnalysisRequest.builder()
                .text(text)
                .language(language != null ? language : "vi")
                .build();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<LangAiAnalysisRequest> entity = new HttpEntity<>(request, headers);
            
            log.debug("Gọi Lang-AI Service để trích xuất cụm từ khóa: {}", url);
            
            ResponseEntity<LangAiKeyPhrasesResponse> response = restTemplate.postForEntity(
                url,
                entity,
                LangAiKeyPhrasesResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.debug("Trích xuất cụm từ khóa thành công, tìm thấy {} cụm từ", 
                    response.getBody().getKeyPhrases().size());
                return response.getBody();
            } else {
                log.error("Phản hồi không mong đợi từ Lang-AI Service: {}", response.getStatusCode());
                throw new RuntimeException("Không thể trích xuất cụm từ khóa");
            }
            
        } catch (RestClientException e) {
            log.error("Lỗi khi gọi Lang-AI Service để trích xuất cụm từ khóa", e);
            throw new RuntimeException("Không thể kết nối với Lang-AI Service", e);
        }
    }
    
    /**
     * Kiểm tra xem Lang-AI Service có khả dụng không
     */
    public boolean isServiceAvailable() {
        try {
            String url = langAiServiceUrl + "/api/v1/health";
            restTemplate.getForEntity(url, String.class);
            return true;
        } catch (Exception e) {
            log.warn("Lang-AI Service không khả dụng: {}", e.getMessage());
            return false;
        }
    }
}
