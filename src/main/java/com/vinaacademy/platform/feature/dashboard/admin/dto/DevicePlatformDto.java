package com.vinaacademy.platform.feature.dashboard.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho thống kê thiết bị truy cập
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DevicePlatformDto {
    
    /**
     * % người dùng từ mobile
     */
    private Double mobilePercentage;
    
    /**
     * % người dùng từ desktop
     */
    private Double desktopPercentage;
    
    /**
     * % người dùng từ tablet
     */
    private Double tabletPercentage;
}
