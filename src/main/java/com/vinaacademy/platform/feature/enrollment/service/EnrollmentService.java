package com.vinaacademy.platform.feature.enrollment.service;

import com.vinaacademy.platform.feature.enrollment.dto.EnrollmentRequest;
import com.vinaacademy.platform.feature.enrollment.dto.EnrollmentResponse;
import com.vinaacademy.platform.feature.enrollment.enums.ProgressStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import com.vinaacademy.platform.feature.common.response.PaginationResponse;
import com.vinaacademy.platform.feature.enrollment.dto.StudentProgressDto;

import java.util.List;
import java.util.UUID;

public interface EnrollmentService {

    /**
     * Đăng ký khóa học mới cho người dùng
     * @param request Thông tin yêu cầu đăng ký
     * @param userId ID của người dùng
     * @return Thông tin đăng ký khóa học
     */
    EnrollmentResponse enrollCourse(EnrollmentRequest request, UUID userId);

    /**
     * Lấy thông tin đăng ký khóa học
     * @param enrollmentId ID đăng ký
     * @return Thông tin đăng ký khóa học
     */
    EnrollmentResponse getEnrollment(Long enrollmentId);

    /**
     * Kiểm tra người dùng đã đăng ký khóa học chưa
     * @param userId ID người dùng
     * @param courseId ID khóa học
     * @return true nếu đã đăng ký, false nếu chưa
     */
    boolean isEnrolled(UUID userId, UUID courseId);

    /**
     * Lấy danh sách tất cả khóa học đã đăng ký của người dùng
     * @param userId ID người dùng
     * @return Danh sách khóa học đã đăng ký
     */
    List<EnrollmentResponse> getUserEnrollments(UUID userId);

    /**
     * Lấy danh sách khóa học đã đăng ký của người dùng (có phân trang)
     * @param userId ID người dùng
     * @param pageable Thông tin phân trang
     * @return Page chứa danh sách khóa học đã đăng ký
     */
    Page<EnrollmentResponse> getUserEnrollments(UUID userId, Pageable pageable);

    /**
     * Lấy danh sách khóa học đã đăng ký của người dùng theo trạng thái
     * @param userId ID người dùng
     * @param status Trạng thái tiến độ
     * @return Danh sách khóa học đã đăng ký theo trạng thái
     */
    List<EnrollmentResponse> getUserEnrollmentsByStatus(UUID userId, ProgressStatus status);

    /**
     * Cập nhật tiến độ học tập
     * @param enrollmentId ID đăng ký
     * @param progressPercentage Phần trăm tiến độ mới
     * @return Thông tin đăng ký đã cập nhật
     */
    EnrollmentResponse updateProgress(Long enrollmentId, Double progressPercentage);

    /**
     * Cập nhật trạng thái học tập
     * @param enrollmentId ID đăng ký
     * @param status Trạng thái mới
     * @return Thông tin đăng ký đã cập nhật
     */
    EnrollmentResponse updateStatus(Long enrollmentId, ProgressStatus status);

    /**
     * Hủy đăng ký khóa học
     * @param enrollmentId ID đăng ký
     */
    void cancelEnrollment(Long enrollmentId);

    /**
     * Lấy danh sách học viên đã đăng ký một khóa học
     * @param courseId ID khóa học
     * @param status Trạng thái tiến độ (tùy chọn)
     * @param pageable Thông tin phân trang
     * @return Page chứa danh sách đăng ký của khóa học
     */
    Page<EnrollmentResponse> getCourseEnrollments(UUID courseId, ProgressStatus status, Pageable pageable);

    @Transactional(readOnly = true)
    boolean isEnrollmentOwnerByUser(Long enrollmentId, UUID userId);

    @Transactional(readOnly = true)
    boolean isCourseOwnerByInstructor(UUID CourseId, UUID instructorId);

    @Transactional(readOnly = true)
    boolean isEnrollmentInCourseOfInstructor(Long enrollmentId, UUID instructorId);

    /**
     * Lấy danh sách học viên của các khóa học mà instructor giảng dạy
     *
     * @param instructorId ID của giảng viên
     * @param courseId ID của khóa học (nullable)
     * @param search Từ khóa tìm kiếm theo tên hoặc email học viên (nullable)
     * @param status Trạng thái tiến độ học tập (nullable)
     * @param pageable Thông tin phân trang
     * @return Trang danh sách học viên và tiến độ học tập
     */
    PaginationResponse<StudentProgressDto> getStudentsProgressForInstructor(
            UUID instructorId,
            UUID courseId,
            String search,
            ProgressStatus status,
            Pageable pageable);

    /**
     * Kiểm tra người dùng có phải là giảng viên của bất kỳ khóa học nào không
     *
     * @param instructorId ID của giảng viên
     * @return true nếu là giảng viên của ít nhất một khóa học
     */
    boolean isInstructor(UUID instructorId);

    /**
     * Lấy danh sách các khóa học mà instructor giảng dạy
     *
     * @param instructorId ID của giảng viên
     * @return Danh sách ID của các khóa học
     */
    List<UUID> getCourseIdsByInstructor(UUID instructorId);
}