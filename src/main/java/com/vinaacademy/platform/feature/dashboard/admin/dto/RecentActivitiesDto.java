package com.vinaacademy.platform.feature.dashboard.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO cho các hoạt động gần đây
 * Bao gồm khóa học mới, giảng viên mới, và đánh giá mới
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentActivitiesDto {
    
    /**
     * Danh sách khóa học gần đây
     */
    private List<RecentCourseDto> recentCourses;
    
    /**
     * Danh sách giảng viên mới
     */
    private List<RecentInstructorDto> recentInstructors;
    
    /**
     * Danh sách đánh giá gần đây
     */
    private List<RecentReviewDto> recentReviews;
}
