package com.vinaacademy.platform.feature.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for course dashboard statistics including total counts and monthly growth percentages
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDashboardStatsDto {
    
    /**
     * Total number of courses (all statuses)
     */
    private long totalCourses;
    
    /**
     * Number of published courses
     */
    private long publishedCourses;
    
    /**
     * Number of pending courses waiting for approval
     */
    private long pendingCourses;
    
    /**
     * Average revenue per course in VND
     */
    private double avgRevenuePerCourse;
    
    /**
     * Monthly growth statistics compared to previous month
     */
    private MonthlyGrowth monthlyGrowth;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyGrowth {
        /**
         * Growth percentage of total courses
         */
        private double courses;
        
        /**
         * Growth percentage of published courses
         */
        private double published;
        
        /**
         * Growth percentage of pending courses
         */
        private double pending;
        
        /**
         * Growth percentage of average revenue per course
         */
        private double revenue;
    }
}
