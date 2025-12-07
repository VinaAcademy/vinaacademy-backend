package com.vinaacademy.platform.feature.common.exception;

/**
 * Exception thrown when Text-to-Speech service encounters an error
 */
public class TTSException extends RuntimeException {
    
    public TTSException(String message) {
        super(message);
    }
    
    public TTSException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static TTSException serviceUnavailable() {
        return new TTSException("Text-to-Speech service không khả dụng");
    }
    
    public static TTSException contentTooLong(int maxLength) {
        return new TTSException(
            String.format("Nội dung quá dài. Tối đa %d ký tự", maxLength));
    }
    
    public static TTSException invalidLessonType() {
        return new TTSException("TTS chỉ khả dụng cho bài học Reading");
    }
    
    public static TTSException audioGenerationFailed() {
        return new TTSException("Không thể tạo audio từ văn bản");
    }
}
