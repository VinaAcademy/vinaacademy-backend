package com.vinaacademy.platform.feature.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for course trend statistics over time
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseTrendDto {
    
    /**
     * List of monthly trend data points
     */
    private List<MonthlyTrend> trends;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyTrend {
        /**
         * Month label (e.g., "T1", "T2", etc.)
         */
        private String month;
        
        /**
         * Number of courses created in this month
         */
        private long created;
        
        /**
         * Number of courses published in this month
         */
        private long published;
    }
}
