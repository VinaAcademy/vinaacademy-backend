package com.vinaacademy.platform.feature.instructor.controller;

import com.vinaacademy.platform.feature.common.response.ApiResponse;
import com.vinaacademy.platform.feature.instructor.dto.CourseOverviewDto;
import com.vinaacademy.platform.feature.instructor.dto.DashboardStatisticsDto;
import com.vinaacademy.platform.feature.instructor.dto.RecentActivitiesDto;
import com.vinaacademy.platform.feature.instructor.dto.RevenueChartDto;
import com.vinaacademy.platform.feature.instructor.dto.StudentsChartDto;
import com.vinaacademy.platform.feature.instructor.service.InstructorDashboardService;
import com.vinaacademy.platform.feature.user.auth.annotation.HasAnyRole;
import com.vinaacademy.platform.feature.user.constant.AuthConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller cho instructor dashboard APIs
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/instructor/dashboard")
@RequiredArgsConstructor
@Tag(name = "Instructor Dashboard", description = "APIs for instructor dashboard statistics and analytics")
public class InstructorDashboardController {

    private final InstructorDashboardService dashboardService;

    /**
     * Lấy thống kê tổng quan cho dashboard
     *
     * @param timeRange Khoảng thời gian: week, month, year (default: month)
     * @return DashboardStatisticsDto chứa các thống kê
     */
    @HasAnyRole({AuthConstants.INSTRUCTOR_ROLE, AuthConstants.STAFF_ROLE, AuthConstants.ADMIN_ROLE})
    @GetMapping("/statistics")
    @Operation(
            summary = "Lấy thống kê dashboard",
            description = "Lấy các thống kê tổng quan cho instructor dashboard bao gồm doanh thu, học viên mới, đánh giá"
    )
    public ResponseEntity<ApiResponse<DashboardStatisticsDto>> getDashboardStatistics(
            @RequestParam(defaultValue = "MONTH") String timeRange
    ) {
        log.info("Getting dashboard statistics with timeRange: {}", timeRange);

        DashboardStatisticsDto statistics = dashboardService.getDashboardStatistics(timeRange);

        return ResponseEntity.ok(
                ApiResponse.success("Lấy thống kê dashboard thành công", statistics)
        );
    }

    /**
     * Lấy tổng quan các khóa học của giảng viên
     *
     * @param sortBy Sắp xếp theo: POPULAR (students), RECENT (lastUpdated), REVENUE (default: POPULAR)
     * @return CourseOverviewDto chứa summary và danh sách courses
     */
    @HasAnyRole({AuthConstants.INSTRUCTOR_ROLE, AuthConstants.STAFF_ROLE, AuthConstants.ADMIN_ROLE})
    @GetMapping("/courses-overview")
    @Operation(
            summary = "Lấy tổng quan khóa học",
            description = "Lấy danh sách các khóa học với thống kê chi tiết (students, rating, revenue, completion rate)"
    )
    public ResponseEntity<ApiResponse<CourseOverviewDto>> getCourseOverview(
            @RequestParam(defaultValue = "POPULAR") String sortBy
    ) {
        log.info("Getting course overview with sortBy: {}", sortBy);

        CourseOverviewDto overview = dashboardService.getCourseOverview(sortBy);

        return ResponseEntity.ok(
                ApiResponse.success("Lấy tổng quan khóa học thành công", overview)
        );
    }

    /**
     * Lấy các hoạt động gần đây (đăng ký mới, đánh giá mới)
     *
     * @param limit Số lượng items tối đa cho mỗi loại hoạt động (default: 5)
     * @return RecentActivitiesDto chứa enrollments và reviews
     */
    @HasAnyRole({AuthConstants.INSTRUCTOR_ROLE, AuthConstants.STAFF_ROLE, AuthConstants.ADMIN_ROLE})
    @GetMapping("/recent-activities")
    @Operation(
            summary = "Lấy hoạt động gần đây",
            description = "Lấy danh sách đăng ký mới và đánh giá mới gần đây cho các khóa học của giảng viên"
    )
    public ResponseEntity<ApiResponse<RecentActivitiesDto>> getRecentActivities(
            @RequestParam(defaultValue = "5") Integer limit
    ) {
        log.info("Getting recent activities with limit: {}", limit);

        RecentActivitiesDto activities = dashboardService.getRecentActivities(limit);

        return ResponseEntity.ok(
                ApiResponse.success("Lấy hoạt động gần đây thành công", activities)
        );
    }

    /**
     * Lấy dữ liệu biểu đồ doanh thu theo thời gian
     *
     * @param period Khoảng thời gian: WEEK (7 ngày), MONTH (12 tháng), YEAR (12 tháng theo năm) (default: MONTH)
     * @return RevenueChartDto chứa dữ liệu doanh thu theo thời gian
     */
    @HasAnyRole({AuthConstants.INSTRUCTOR_ROLE, AuthConstants.STAFF_ROLE, AuthConstants.ADMIN_ROLE})
    @GetMapping("/revenue-chart")
    @Operation(
            summary = "Lấy biểu đồ doanh thu",
            description = "Lấy dữ liệu doanh thu theo thời gian để hiển thị biểu đồ"
    )
    public ResponseEntity<ApiResponse<RevenueChartDto>> getRevenueChart(
            @RequestParam(defaultValue = "MONTH") String period
    ) {
        log.info("Getting revenue chart with period: {}", period);

        RevenueChartDto chartData = dashboardService.getRevenueChart(period);

        return ResponseEntity.ok(
                ApiResponse.success("Lấy biểu đồ doanh thu thành công", chartData)
        );
    }

    /**
     * Lấy dữ liệu biểu đồ số lượng học viên theo thời gian
     *
     * @param period Khoảng thời gian: WEEK (7 ngày), MONTH (12 tháng), YEAR (12 tháng theo năm) (default: MONTH)
     * @return StudentsChartDto chứa dữ liệu số lượng học viên theo thời gian
     */
    @HasAnyRole({AuthConstants.INSTRUCTOR_ROLE, AuthConstants.STAFF_ROLE, AuthConstants.ADMIN_ROLE})
    @GetMapping("/students-chart")
    @Operation(
            summary = "Lấy biểu đồ học viên",
            description = "Lấy dữ liệu số lượng học viên mới theo thời gian để hiển thị biểu đồ"
    )
    public ResponseEntity<ApiResponse<StudentsChartDto>> getStudentsChart(
            @RequestParam(defaultValue = "MONTH") String period
    ) {
        log.info("Getting students chart with period: {}", period);

        StudentsChartDto chartData = dashboardService.getStudentsChart(period);

        return ResponseEntity.ok(
                ApiResponse.success("Lấy biểu đồ học viên thành công", chartData)
        );
    }
}
