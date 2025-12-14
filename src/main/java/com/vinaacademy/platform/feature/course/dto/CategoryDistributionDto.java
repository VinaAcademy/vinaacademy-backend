package com.vinaacademy.platform.feature.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for category distribution statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDistributionDto {
    
    /**
     * List of category statistics
     */
    private List<CategoryStats> categories;
    
    /**
     * Total number of courses across all categories
     */
    private long totalCourses;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryStats {
        /**
         * Category ID
         */
        private String categoryId;
        
        /**
         * Category name
         */
        private String categoryName;
        
        /**
         * Category slug
         */
        private String categorySlug;
        
        /**
         * Number of courses in this category
         */
        private long count;
        
        /**
         * Percentage of total courses
         */
        private double percentage;
    }
}
