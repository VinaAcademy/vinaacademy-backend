package com.vinaacademy.platform.feature.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for top courses ranking
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopCoursesDto {
    
    /**
     * List of top courses
     */
    private List<TopCourse> courses;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopCourse {
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
         * Number of enrolled students
         */
        private long students;
        
        /**
         * Average rating
         */
        private double rating;
        
        /**
         * Total revenue generated
         */
        private double revenue;
    }
}
