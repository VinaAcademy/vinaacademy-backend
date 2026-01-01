package com.vinaacademy.platform.feature.dashboard.admin.controller;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vinaacademy.platform.feature.common.response.ApiResponse;
import com.vinaacademy.platform.feature.dashboard.admin.dto.*;
import com.vinaacademy.platform.feature.dashboard.admin.service.AdminDashboardService;
import com.vinaacademy.platform.feature.user.auth.annotation.HasAnyRole;
import com.vinaacademy.platform.feature.user.constant.AuthConstants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller cho Admin Dashboard
 * Cung cấp các endpoints để lấy thống kê và dữ liệu dashboard
 * 
 * <p>Tất cả endpoints yêu cầu role ADMIN hoặc STAFF</p>
 * <p>Sử dụng caching để tối ưu performance</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
@HasAnyRole({AuthConstants.ADMIN_ROLE, AuthConstants.STAFF_ROLE})
public class AdminDashboardController {
    
    private final AdminDashboardService dashboardService;
    
    /**
     * Lấy thống kê tổng quan nền tảng
     * <p>
     * Endpoint: GET /api/v1/admin/dashboard/platform-stats
     * <p>
     * Luồng hoạt động:
     * <ul>
     *   <li>Nhận tham số timeRange (week/month/year)</li>
     *   <li>Gọi service để tính toán thống kê</li>
     *   <li>Trả về dữ liệu với cache 5 phút</li>
     * </ul>
     * 
     * @param timeRange Khoảng thời gian: "week", "month", hoặc "year" (mặc định: "month")
     * @return ResponseEntity chứa PlatformStatsDto
     */
    @GetMapping("/platform-stats")
    @Cacheable(value = "platformStats", key = "#timeRange", unless = "#result == null")
    public ResponseEntity<ApiResponse<PlatformStatsDto>> getPlatformStats(
            @RequestParam(defaultValue = "month") String timeRange) {
        
        log.info("Fetching platform stats for time range: {}", timeRange);
        
        try {
            PlatformStatsDto stats = dashboardService.getPlatformStats(timeRange);
            
            return ResponseEntity.ok(
                ApiResponse.success("Platform statistics retrieved successfully", stats)
            );
        } catch (Exception e) {
            log.error("Error fetching platform stats: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Lấy tổng quan doanh thu
     * <p>
     * Endpoint: GET /api/v1/admin/dashboard/revenue-overview
     * <p>
     * Luồng hoạt động:
     * <ul>
     *   <li>Lấy dữ liệu doanh thu 12 tháng gần nhất</li>
     *   <li>Tính phân bố theo danh mục</li>
     *   <li>Tính tổng và các chỉ số liên quan</li>
     * </ul>
     * 
     * @return ResponseEntity chứa RevenueOverviewDto
     */
    @GetMapping("/revenue-overview")
    @Cacheable(value = "revenueOverview", unless = "#result == null")
    public ResponseEntity<ApiResponse<RevenueOverviewDto>> getRevenueOverview() {
        
        log.info("Fetching revenue overview");
        
        try {
            RevenueOverviewDto overview = dashboardService.getRevenueOverview();
            
            return ResponseEntity.ok(
                ApiResponse.success("Revenue overview retrieved successfully", overview)
            );
        } catch (Exception e) {
            log.error("Error fetching revenue overview: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Lấy thống kê người dùng hoạt động
     * <p>
     * Endpoint: GET /api/v1/admin/dashboard/active-users
     * <p>
     * Luồng hoạt động:
     * <ul>
     *   <li>Lấy số liệu người dùng theo tháng (12 tháng)</li>
     *   <li>Phân tích theo role (student/instructor)</li>
     *   <li>Tính retention rate</li>
     *   <li>Thống kê theo thiết bị</li>
     * </ul>
     * 
     * @return ResponseEntity chứa ActiveUsersDto
     */
    @GetMapping("/active-users")
    @Cacheable(value = "activeUsers", unless = "#result == null")
    public ResponseEntity<ApiResponse<ActiveUsersDto>> getActiveUsers() {
        
        log.info("Fetching active users statistics");
        
        try {
            ActiveUsersDto users = dashboardService.getActiveUsers();
            
            return ResponseEntity.ok(
                ApiResponse.success("Active users data retrieved successfully", users)
            );
        } catch (Exception e) {
            log.error("Error fetching active users: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Lấy các hoạt động gần đây
     * <p>
     * Endpoint: GET /api/v1/admin/dashboard/recent-activities
     * <p>
     * Luồng hoạt động:
     * <ul>
     *   <li>Lấy 5 khóa học mới nhất</li>
     *   <li>Lấy 5 giảng viên mới nhất</li>
     *   <li>Lấy 5 đánh giá mới nhất</li>
     * </ul>
     * 
     * @return ResponseEntity chứa RecentActivitiesDto
     */
    @GetMapping("/recent-activities")
    @Cacheable(value = "recentActivities", unless = "#result == null")
    public ResponseEntity<ApiResponse<RecentActivitiesDto>> getRecentActivities() {
        
        log.info("Fetching recent activities");
        
        try {
            RecentActivitiesDto activities = dashboardService.getRecentActivities();
            
            return ResponseEntity.ok(
                ApiResponse.success("Recent activities retrieved successfully", activities)
            );
        } catch (Exception e) {
            log.error("Error fetching recent activities: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Lấy số liệu cho quick actions
     * <p>
     * Endpoint: GET /api/v1/admin/dashboard/quick-actions
     * <p>
     * Luồng hoạt động:
     * <ul>
     *   <li>Đếm số khóa học chờ duyệt</li>
     *   <li>Đếm số yêu cầu rút tiền chờ xử lý</li>
     *   <li>Đếm số báo cáo vi phạm</li>
     *   <li>Đếm số yêu cầu hỗ trợ</li>
     * </ul>
     * 
     * @return ResponseEntity chứa QuickActionsDto
     */
    @GetMapping("/quick-actions")
    @Cacheable(value = "quickActions", unless = "#result == null")
    public ResponseEntity<ApiResponse<QuickActionsDto>> getQuickActions() {
        
        log.info("Fetching quick actions counts");
        
        try {
            QuickActionsDto actions = dashboardService.getQuickActions();
            
            return ResponseEntity.ok(
                ApiResponse.success("Quick actions retrieved successfully", actions)
            );
        } catch (Exception e) {
            log.error("Error fetching quick actions: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
