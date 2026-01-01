package com.vinaacademy.platform.feature.dashboard.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO cho thống kê người dùng hoạt động
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActiveUsersDto {
    
    /**
     * Dữ liệu người dùng theo tháng (12 tháng)
     */
    private List<MonthlyUsersDto> monthlyData;
    
    /**
     * Tổng số người dùng
     */
    private Long totalUsers;
    
    /**
     * % tăng trưởng người dùng
     */
    private Double userGrowth;
    
    /**
     * Số lượng học viên
     */
    private Long studentCount;
    
    /**
     * % học viên so với tổng
     */
    private Double studentPercentage;
    
    /**
     * Số lượng giảng viên
     */
    private Long instructorCount;
    
    /**
     * % giảng viên so với tổng
     */
    private Double instructorPercentage;
    
    /**
     * Tỷ lệ giữ chân người dùng (%)
     */
    private Double retentionRate;
    
    /**
     * Thống kê theo thiết bị
     */
    private DevicePlatformDto deviceStats;
}
