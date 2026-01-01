package com.vinaacademy.platform.feature.dashboard.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO cho đánh giá gần đây
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentReviewDto {
    
    /**
     * Tên học viên
     */
    private String studentName;
    
    /**
     * Chữ cái đầu tên học viên
     */
    private String studentInitials;
    
    /**
     * Số sao đánh giá (1-5)
     */
    private Integer rating;
    
    /**
     * Nội dung đánh giá
     */
    private String comment;
    
    /**
     * Tên khóa học được đánh giá
     */
    private String courseTitle;
    
    /**
     * Thời gian đánh giá
     */
    private LocalDateTime createdAt;
}
