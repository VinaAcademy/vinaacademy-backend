package com.vinaacademy.platform.feature.lesson.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Text-to-Speech request from client
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TTSRequestDto {
    
    /**
     * Voice to use for synthesis
     * Default: vi-VN-HoaiMyNeural (Vietnamese female)
     */
    private String voice;
    
    /**
     * Speech speed (0.5 to 2.0)
     * 1.0 is normal speed
     */
    @Min(value = 0, message = "Speed phải từ 0.5 đến 2.0")
    @Max(value = 2, message = "Speed phải từ 0.5 đến 2.0")
    private String speed;
    
    /**
     * Pitch adjustment (-50Hz to +50Hz)
     */
    private String pitch;
    
    /**
     * Response format: "base64" or "stream"
     * base64: Returns audio as base64 string in response body
     * stream: Returns audio as streaming response
     */
    @Pattern(regexp = "base64|stream", message = "Format phải là 'base64' hoặc 'stream'")
    private String format;
    
    /**
     * Set default values
     */
    public String getVoice() {
        return voice != null ? voice : "vi-VN-HoaiMyNeural";
    }
    
    public String getSpeed() {
        return speed != null ? speed : "1.0";
    }
    
    public String getPitch() {
        return pitch != null ? pitch : "0Hz";
    }
    
    public String getFormat() {
        return format != null ? format : "base64";
    }
}
