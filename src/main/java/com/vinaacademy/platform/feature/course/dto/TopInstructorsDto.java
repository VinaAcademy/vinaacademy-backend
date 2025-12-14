package com.vinaacademy.platform.feature.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for top instructors ranking
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopInstructorsDto {
    
    /**
     * List of top instructors
     */
    private List<TopInstructor> instructors;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopInstructor {
        /**
         * Instructor ID
         */
        private String instructorId;
        
        /**
         * Instructor name
         */
        private String name;
        
        /**
         * Number of courses created
         */
        private long courseCount;
        
        /**
         * Total students across all courses
         */
        private long totalStudents;
        
        /**
         * Average rating across all courses
         */
        private double avgRating;
        
        /**
         * Total revenue generated
         */
        private double revenue;
    }
}
