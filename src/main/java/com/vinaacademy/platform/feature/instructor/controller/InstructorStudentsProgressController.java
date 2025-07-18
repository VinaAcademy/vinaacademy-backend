package com.vinaacademy.platform.feature.instructor.controller;

import com.vinaacademy.platform.feature.common.response.ApiResponse;
import com.vinaacademy.platform.feature.common.response.PaginationResponse;
import com.vinaacademy.platform.feature.enrollment.dto.StudentProgressDto;
import com.vinaacademy.platform.feature.enrollment.enums.ProgressStatus;
import com.vinaacademy.platform.feature.enrollment.service.EnrollmentService;
import com.vinaacademy.platform.feature.user.auth.annotation.HasAnyRole;
import com.vinaacademy.platform.feature.user.auth.helpers.SecurityHelper;
import com.vinaacademy.platform.feature.user.constant.AuthConstants;
import com.vinaacademy.platform.feature.user.entity.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/instructor/courses")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "InstructorCourseStudents", description = "Quản lý tiến độ học viên")
public class InstructorStudentsProgressController {

    private final EnrollmentService enrollmentService;
    private final SecurityHelper securityHelper;

    /**
     * Xem tiến độ học tập của học viên
     */
    @HasAnyRole({AuthConstants.INSTRUCTOR_ROLE})
    @GetMapping("/students-progress")
    @Operation(summary = "Danh sách tiến độ học viên", description = "Xem tiến độ học tập của học viên trong các khóa học của giảng viên")
    public ApiResponse<PaginationResponse<StudentProgressDto>> getStudentsProgress(
            @RequestParam(required = false) UUID courseId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ProgressStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        log.debug("Lấy danh sách tiến độ học viên với params: courseId={}, search={}, status={}, page={}, size={}, sortBy={}, sortDirection={}",
                courseId, search, status, page, size, sortBy, sortDirection);

        // Lấy thông tin người dùng hiện tại
        User currentUser = securityHelper.getCurrentUser();
        UUID instructorId = currentUser.getId();

        // Kiểm tra xem người dùng có phải là giảng viên không
        if (!enrollmentService.isInstructor(instructorId)) {
            return ApiResponse.error("Bạn không có quyền xem danh sách tiến độ học viên");
        }

        // Tạo thông tin phân trang
        Sort sort = "asc".equalsIgnoreCase(sortDirection)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        try {
            // Lấy danh sách học viên và tiến độ học tập
            PaginationResponse<StudentProgressDto> paginationResponse = enrollmentService.getStudentsProgressForInstructor(
                    instructorId, courseId, search, status, pageable);

            // Trả về kết quả
            return ApiResponse.success(paginationResponse);
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách tiến độ học viên: {}", e.getMessage(), e);
            return ApiResponse.error("Lỗi khi lấy danh sách tiến độ học viên: " + e.getMessage());
        }
    }

    /**
     * Xem tiến độ học tập của học viên theo khóa học cụ thể
     */
    @HasAnyRole({AuthConstants.INSTRUCTOR_ROLE})
    @GetMapping("/{courseId}/students-progress")
    @Operation(summary = "Danh sách tiến độ học viên theo khóa học", description = "Xem tiến độ học tập của học viên trong một khóa học cụ thể")
    public ApiResponse<PaginationResponse<StudentProgressDto>> getStudentsProgressByCourse(
            @PathVariable UUID courseId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ProgressStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        log.debug("Lấy danh sách tiến độ học viên theo khóa học với params: courseId={}, search={}, status={}, page={}, size={}, sortBy={}, sortDirection={}",
                courseId, search, status, page, size, sortBy, sortDirection);

        // Lấy thông tin người dùng hiện tại
        User currentUser = securityHelper.getCurrentUser();
        UUID instructorId = currentUser.getId();

        // Kiểm tra xem người dùng có quyền xem khóa học này không
        if (!enrollmentService.isCourseOwnerByInstructor(courseId, instructorId)) {
            return ApiResponse.error("Bạn không có quyền xem danh sách tiến độ học viên của khóa học này");
        }

        // Tạo thông tin phân trang
        Sort sort = "asc".equalsIgnoreCase(sortDirection)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        try {
            // Lấy danh sách học viên và tiến độ học tập
            PaginationResponse<StudentProgressDto> paginationResponse = enrollmentService.getStudentsProgressForInstructor(
                    instructorId, courseId, search, status, pageable);

            // Trả về kết quả
            return ApiResponse.success(paginationResponse);
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách tiến độ học viên theo khóa học: {}", e.getMessage(), e);
            return ApiResponse.error("Lỗi khi lấy danh sách tiến độ học viên theo khóa học: " + e.getMessage());
        }
    }
}