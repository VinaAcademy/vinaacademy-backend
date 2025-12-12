package com.vinaacademy.platform.feature.instructor.controller;

import com.vinaacademy.platform.feature.common.response.ApiResponse;
import com.vinaacademy.platform.feature.enrollment.enums.ProgressStatus;
import com.vinaacademy.platform.feature.instructor.dto.StudentDetailDto;
import com.vinaacademy.platform.feature.instructor.dto.StudentsOverviewDto;
import com.vinaacademy.platform.feature.instructor.dto.StudentsProgressChartDto;
import com.vinaacademy.platform.feature.instructor.service.InstructorStudentsService;
import com.vinaacademy.platform.feature.user.auth.annotation.HasAnyRole;
import com.vinaacademy.platform.feature.user.constant.AuthConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller cho quản lý học viên của instructor
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/instructor/students")
@RequiredArgsConstructor
@Tag(name = "Instructor Students", description = "APIs quản lý học viên cho giảng viên")
public class InstructorStudentsController {

    private final InstructorStudentsService studentsService;

    /**
     * Lấy tổng quan học viên
     *
     * @param timeRange Khoảng thời gian: WEEK, MONTH, YEAR (default: MONTH)
     * @return StudentsOverviewDto chứa các thống kê tổng quan
     */
    @HasAnyRole({AuthConstants.INSTRUCTOR_ROLE, AuthConstants.STAFF_ROLE, AuthConstants.ADMIN_ROLE})
    @GetMapping("/overview")
    @Operation(
            summary = "Lấy tổng quan học viên",
            description = "Lấy các thống kê tổng quan về học viên của instructor"
    )
    public ResponseEntity<ApiResponse<StudentsOverviewDto>> getStudentsOverview(
            @RequestParam(defaultValue = "MONTH") String timeRange
    ) {
        log.info("Getting students overview with timeRange: {}", timeRange);

        StudentsOverviewDto overview = studentsService.getStudentsOverview(timeRange);

        return ResponseEntity.ok(
                ApiResponse.success("Lấy tổng quan học viên thành công", overview)
        );
    }

    /**
     * Lấy danh sách học viên với phân trang và filter
     *
     * @param courseId      Filter theo khóa học (optional)
     * @param status        Filter theo trạng thái (optional)
     * @param minProgress   Filter theo tiến độ tối thiểu (optional)
     * @param maxProgress   Filter theo tiến độ tối đa (optional)
     * @param keyword       Tìm kiếm theo tên/email (optional)
     * @param pageable      Thông tin phân trang
     * @return Page chứa danh sách StudentDetailDto
     */
    @HasAnyRole({AuthConstants.INSTRUCTOR_ROLE, AuthConstants.STAFF_ROLE, AuthConstants.ADMIN_ROLE})
    @GetMapping
    @Operation(
            summary = "Lấy danh sách học viên",
            description = "Lấy danh sách học viên với filter và phân trang",
            parameters = {
                    @Parameter(name = "courseId", description = "Filter theo khóa học", example = "550e8400-e29b-41d4-a716-446655440000"),
                    @Parameter(name = "status", description = "Filter theo trạng thái: IN_PROGRESS, COMPLETED, NOT_STARTED, DROPPED"),
                    @Parameter(name = "minProgress", description = "Tiến độ tối thiểu (%)", example = "0"),
                    @Parameter(name = "maxProgress", description = "Tiến độ tối đa (%)", example = "100"),
                    @Parameter(name = "keyword", description = "Tìm kiếm theo tên/email", example = "nguyen"),
                    @Parameter(name = "page", description = "Số trang (0-based)", example = "0"),
                    @Parameter(name = "size", description = "Số items mỗi trang", example = "10"),
                    @Parameter(name = "sort", description = "Sắp xếp", example = "lastActive,desc")
            }
    )
    public ResponseEntity<ApiResponse<Page<StudentDetailDto>>> getStudentsList(
            @RequestParam(required = false) String courseId,
            @RequestParam(required = false) ProgressStatus status,
            @RequestParam(required = false) Double minProgress,
            @RequestParam(required = false) Double maxProgress,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        log.info("Getting students list with filters - courseId: {}, status: {}, keyword: {}",
                courseId, status, keyword);

        Page<StudentDetailDto> students = studentsService.getStudentsList(
                courseId, status, minProgress, maxProgress, keyword, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Lấy danh sách học viên thành công", students)
        );
    }

    /**
     * Lấy biểu đồ tiến độ học viên theo thời gian
     *
     * @param period Khoảng thời gian: WEEK (7 ngày), MONTH (12 tháng), YEAR (12 tháng theo năm) (default: MONTH)
     * @return StudentsProgressChartDto chứa dữ liệu biểu đồ
     */
    @HasAnyRole({AuthConstants.INSTRUCTOR_ROLE, AuthConstants.STAFF_ROLE, AuthConstants.ADMIN_ROLE})
    @GetMapping("/progress-chart")
    @Operation(
            summary = "Lấy biểu đồ tiến độ học viên",
            description = "Lấy dữ liệu biểu đồ tiến độ học viên theo thời gian"
    )
    public ResponseEntity<ApiResponse<StudentsProgressChartDto>> getStudentsProgressChart(
            @RequestParam(defaultValue = "MONTH") String period
    ) {
        log.info("Getting students progress chart with period: {}", period);

        StudentsProgressChartDto chartData = studentsService.getStudentsProgressChart(period);

        return ResponseEntity.ok(
                ApiResponse.success("Lấy biểu đồ tiến độ học viên thành công", chartData)
        );
    }
}
