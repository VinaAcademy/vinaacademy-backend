package com.vinaacademy.platform.feature.instructor.service;

import com.vinaacademy.platform.feature.enrollment.enums.ProgressStatus;
import com.vinaacademy.platform.feature.instructor.dto.StudentDetailDto;
import com.vinaacademy.platform.feature.instructor.dto.StudentsOverviewDto;
import com.vinaacademy.platform.feature.instructor.dto.StudentsProgressChartDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface cho quản lý học viên của instructor
 */
public interface InstructorStudentsService {

    /**
     * Lấy tổng quan học viên của instructor
     *
     * @param timeRange Khoảng thời gian: WEEK, MONTH, YEAR
     * @return StudentsOverviewDto chứa các thống kê tổng quan
     */
    StudentsOverviewDto getStudentsOverview(String timeRange);

    /**
     * Lấy danh sách học viên chi tiết với phân trang và filter
     *
     * @param courseId        Filter theo khóa học (optional)
     * @param status          Filter theo trạng thái (optional)
     * @param minProgress     Filter theo tiến độ tối thiểu (optional)
     * @param maxProgress     Filter theo tiến độ tối đa (optional)
     * @param searchKeyword   Tìm kiếm theo tên/email (optional)
     * @param pageable        Thông tin phân trang
     * @return Page chứa danh sách StudentDetailDto
     */
    Page<StudentDetailDto> getStudentsList(
            String courseId,
            ProgressStatus status,
            Double minProgress,
            Double maxProgress,
            String searchKeyword,
            Pageable pageable
    );

    /**
     * Lấy dữ liệu biểu đồ tiến độ học viên theo thời gian
     *
     * @param period Khoảng thời gian: WEEK (7 ngày), MONTH (12 tháng), YEAR (12 tháng theo năm)
     * @return StudentsProgressChartDto chứa dữ liệu biểu đồ
     */
    StudentsProgressChartDto getStudentsProgressChart(String period);
}
