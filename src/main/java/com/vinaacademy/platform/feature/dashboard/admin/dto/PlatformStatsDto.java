package com.vinaacademy.platform.feature.dashboard.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO cho thống kê tổng quan nền tảng
 * Bao gồm tổng số users, courses, instructors, revenue và % thay đổi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformStatsDto {
    
    /**
     * Tổng số người dùng
     */
    private Long totalUsers;
    
    /**
     * % thay đổi số người dùng so với kỳ trước
     */
    private Double userChange;
    
    /**
     * Tổng số khóa học
     */
    private Long totalCourses;
    
    /**
     * % thay đổi số khóa học so với kỳ trước
     */
    private Double courseChange;
    
    /**
     * Tổng số giảng viên
     */
    private Long totalInstructors;
    
    /**
     * % thay đổi số giảng viên so với kỳ trước
     */
    private Double instructorChange;
    
    /**
     * Tổng doanh thu (VNĐ)
     */
    private BigDecimal totalRevenue;
    
    /**
     * % thay đổi doanh thu so với kỳ trước
     */
    private Double revenueChange;
    
    /**
     * Khoảng thời gian: "week", "month", "year"
     */
    private String timeRange;
}
