package com.vinaacademy.platform.feature.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for alerts and performance metrics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertsAndMetricsDto {
    
    /**
     * Performance metrics
     */
    private Metrics metrics;
    
    /**
     * List of alerts
     */
    private List<Alert> alerts;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Metrics {
        /**
         * Approval rate percentage
         */
        private double approvalRate;
        
        /**
         * Average approval time in days
         */
        private double avgApprovalTime;
        
        /**
         * Rejection rate percentage
         */
        private double rejectionRate;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Alert {
        /**
         * Alert ID
         */
        private String id;
        
        /**
         * Alert type: warning, error, info
         */
        private String type;
        
        /**
         * Alert message
         */
        private String message;
        
        /**
         * Number of items
         */
        private int count;
        
        /**
         * Optional action text
         */
        private String action;
        
        /**
         * Optional link
         */
        private String link;
    }
}
