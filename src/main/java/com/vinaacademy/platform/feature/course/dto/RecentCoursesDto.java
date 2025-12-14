package com.vinaacademy.platform.feature.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for recent published courses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentCoursesDto {
    
    /**
     * List of recent courses
     */
    private List<RecentCourse> courses;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentCourse {
        /**
         * Course ID
         */
        private String id;
        
        /**
         * Course title
         */
        private String title;
        
        /**
         * Primary instructor name
         */
        private String instructor;
        
        /**
         * Course thumbnail image URL
         */
        private String thumbnail;
        
        /**
         * Published date (ISO format)
         */
        private String publishedDate;
        
        /**
         * Category name
         */
        private String category;
    }
}
