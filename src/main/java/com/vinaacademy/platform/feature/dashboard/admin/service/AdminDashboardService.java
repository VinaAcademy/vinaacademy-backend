package com.vinaacademy.platform.feature.dashboard.admin.service;

import com.vinaacademy.platform.feature.dashboard.admin.dto.*;

/**
 * Service interface cho Admin Dashboard
 * Cung cấp các methods để lấy thống kê và dữ liệu dashboard
 */
public interface AdminDashboardService {
    
    /**
     * Lấy thống kê tổng quan nền tảng
     * 
     * @param timeRange Khoảng thời gian: "week", "month", "year"
     * @return PlatformStatsDto chứa thống kê users, courses, instructors, revenue
     */
    PlatformStatsDto getPlatformStats(String timeRange);
    
    /**
     * Lấy tổng quan doanh thu
     * Bao gồm dữ liệu 12 tháng và phân bố theo danh mục
     * 
     * @return RevenueOverviewDto
     */
    RevenueOverviewDto getRevenueOverview();
    
    /**
     * Lấy thống kê người dùng hoạt động
     * Bao gồm dữ liệu 12 tháng và phân tích theo role
     * 
     * @return ActiveUsersDto
     */
    ActiveUsersDto getActiveUsers();
    
    /**
     * Lấy các hoạt động gần đây
     * Bao gồm khóa học mới, giảng viên mới, đánh giá mới
     * 
     * @return RecentActivitiesDto
     */
    RecentActivitiesDto getRecentActivities();
    
    /**
     * Lấy số liệu cho quick actions
     * 
     * @return QuickActionsDto chứa số lượng các items cần xử lý
     */
    QuickActionsDto getQuickActions();
}
