package com.vinaacademy.platform.feature.dashboard.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO cho khóa học gần đây
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentCourseDto {
    
    /**
     * ID khóa học
     */
    private String id;
    
    /**
     * Tiêu đề khóa học
     */
    private String title;
    
    /**
     * URL thumbnail
     */
    private String thumbnail;
    
    /**
     * Số lượng đăng ký
     */
    private Integer enrollmentCount;
    
    /**
     * Trạng thái: "APPROVED", "PENDING", "REJECTED"
     */
    private String status;
    
    /**
     * Thời gian tạo
     */
    private LocalDateTime createdAt;
}
