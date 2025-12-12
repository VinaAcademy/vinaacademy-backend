package com.vinaacademy.platform.feature.instructor.service;

import com.vinaacademy.platform.feature.instructor.dto.CourseOverviewDto;
import com.vinaacademy.platform.feature.instructor.dto.DashboardStatisticsDto;
import com.vinaacademy.platform.feature.instructor.dto.RecentActivitiesDto;
import com.vinaacademy.platform.feature.instructor.dto.RevenueChartDto;
import com.vinaacademy.platform.feature.instructor.dto.StudentsChartDto;

/**
 * Service interface cho instructor dashboard
 */
public interface InstructorDashboardService {

    /**
     * Lấy thống kê tổng quan cho dashboard
     *
     * @param timeRange Khoảng thời gian: WEEK, MONTH, YEAR
     * @return DashboardStatisticsDto chứa các thống kê
     */
    DashboardStatisticsDto getDashboardStatistics(String timeRange);

    /**
     * Lấy tổng quan các khóa học của giảng viên
     *
     * @param sortBy Sắp xếp theo: POPULAR, RECENT, REVENUE
     * @return CourseOverviewDto chứa summary và danh sách courses
     */
    CourseOverviewDto getCourseOverview(String sortBy);

    /**
     * Lấy các hoạt động gần đây (đăng ký mới, đánh giá mới)
     *
     * @param limit Số lượng items tối đa cho mỗi loại hoạt động
     * @return RecentActivitiesDto chứa enrollments và reviews
     */
    RecentActivitiesDto getRecentActivities(Integer limit);

    /**
     * Lấy dữ liệu biểu đồ doanh thu theo thời gian
     *
     * @param period Khoảng thời gian: WEEK (7 ngày), MONTH (12 tháng), YEAR (12 tháng theo năm)
     * @return RevenueChartDto chứa dữ liệu doanh thu theo thời gian
     */
    RevenueChartDto getRevenueChart(String period);

    /**
     * Lấy dữ liệu biểu đồ số lượng học viên theo thời gian
     *
     * @param period Khoảng thời gian: WEEK (7 ngày), MONTH (12 tháng), YEAR (12 tháng theo năm)
     * @return StudentsChartDto chứa dữ liệu số lượng học viên theo thời gian
     */
    StudentsChartDto getStudentsChart(String period);
}
