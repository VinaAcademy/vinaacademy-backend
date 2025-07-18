package com.vinaacademy.platform.feature.enrollment.service;

import com.vinaacademy.platform.exception.BadRequestException;
import com.vinaacademy.platform.feature.common.exception.ResourceNotFoundException;
import com.vinaacademy.platform.feature.common.response.PaginationResponse;
import com.vinaacademy.platform.feature.course.entity.Course;
import com.vinaacademy.platform.feature.course.enums.CourseStatus;
import com.vinaacademy.platform.feature.course.repository.CourseRepository;
import com.vinaacademy.platform.feature.enrollment.Enrollment;
import com.vinaacademy.platform.feature.enrollment.dto.EnrollmentRequest;
import com.vinaacademy.platform.feature.enrollment.dto.EnrollmentResponse;
import com.vinaacademy.platform.feature.enrollment.dto.StudentProgressDto;
import com.vinaacademy.platform.feature.enrollment.enums.ProgressStatus;
import com.vinaacademy.platform.feature.enrollment.mapper.EnrollmentMapper;
import com.vinaacademy.platform.feature.enrollment.repository.EnrollmentRepository;
import com.vinaacademy.platform.feature.user.UserRepository;
import com.vinaacademy.platform.feature.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.data.jpa.domain.Specification;
import java.util.stream.Collectors;
import com.vinaacademy.platform.feature.enrollment.mapper.StudentProgressMapper;
import com.vinaacademy.platform.feature.instructor.repository.CourseInstructorRepository;
import com.vinaacademy.platform.feature.order_payment.entity.Order;
import com.vinaacademy.platform.feature.order_payment.enums.OrderStatus;
import com.vinaacademy.platform.feature.order_payment.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

	private final EnrollmentRepository enrollmentRepository;
	private final CourseRepository courseRepository;
	private final UserRepository userRepository;
	private final EnrollmentMapper enrollmentMapper;
	private final CourseInstructorRepository courseInstructorRepository;
	private final StudentProgressMapper studentProgressMapper;
	private final OrderRepository orderRepository;

	@Override
	@Transactional
	public EnrollmentResponse enrollCourse(EnrollmentRequest request, UUID userId) {
		// Kiểm tra xem người dùng đã đăng ký khóa học này chưa
		if (enrollmentRepository.existsByUserIdAndCourseId(userId, request.getCourseId())) {
			throw new IllegalStateException("Bạn đã đăng ký khóa học này rồi");
		}

		// Lấy thông tin người dùng
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

		// Lấy thông tin khóa học
		Course course = courseRepository.findById(request.getCourseId())
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học"));

		if (course.getStatus() != CourseStatus.PUBLISHED)
			throw BadRequestException.message("Khóa học này chưa được xuất bản");

		if (course.getPrice().longValue() != 0) {
			// because price > 0 so check if user has order complete for this course (anti bypass course)
			Optional<Order> order = orderRepository.findFirstByUser_IdAndOrderItems_Course_IdAndStatusOrderByCreatedDateAsc(userId, course.getId(),
					OrderStatus.PAID);

			if (!order.isPresent()) {
				// if no order == PAID 
				throw BadRequestException.message("Bạn chưa thanh toán cho khóa học này");
			} 
		}

		// Tạo đăng ký mới
		Enrollment enrollment = Enrollment.builder().user(user).course(course).progressPercentage(0.0)
				.status(ProgressStatus.IN_PROGRESS).startAt(LocalDateTime.now()).build();

		// Lưu vào database
		Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

		// Cập nhật số lượng học viên của khóa học
		course.setTotalStudent(course.getTotalStudent() + 1);
		courseRepository.save(course);

		// Chuyển đổi và trả về response
		return enrollmentMapper.toDto(savedEnrollment);
	}

	@Override
	@Transactional(readOnly = true)
	public EnrollmentResponse getEnrollment(Long enrollmentId) {
		Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đăng ký khóa học"));
		return enrollmentMapper.toDto(enrollment);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean isEnrolled(UUID userId, UUID courseId) {
		return enrollmentRepository.existsByUserIdAndCourseId(userId, courseId);
	}

	@Override
	public List<EnrollmentResponse> getUserEnrollments(UUID userId) {
		List<Enrollment> enrollments = enrollmentRepository.findByUserId(userId);
		return enrollments.stream().map(enrollmentMapper::toDto).collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public Page<EnrollmentResponse> getUserEnrollments(UUID userId, Pageable pageable) {
		Page<Enrollment> enrollments = enrollmentRepository.findByUserId(userId, pageable);
		return enrollments.map(enrollmentMapper::toDto);
	}

	@Override
	@Transactional(readOnly = true)
	public List<EnrollmentResponse> getUserEnrollmentsByStatus(UUID userId, ProgressStatus status) {
		List<Enrollment> enrollments = enrollmentRepository.findByUserIdAndStatus(userId, status);
		return enrollments.stream().map(enrollmentMapper::toDto).collect(Collectors.toList());
	}

	@Override
	@Transactional
	public EnrollmentResponse updateProgress(Long enrollmentId, Double progressPercentage) {
		Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đăng ký khóa học"));

		enrollment.setProgressPercentage(progressPercentage);

		// Nếu tiến độ đạt 100%, đánh dấu là đã hoàn thành
		if (progressPercentage >= 100.0) {
			enrollment.setStatus(ProgressStatus.COMPLETED);
			enrollment.setCompleteAt(LocalDateTime.now());
		}

		Enrollment updatedEnrollment = enrollmentRepository.save(enrollment);
		return enrollmentMapper.toDto(updatedEnrollment);
	}

	@Override
	@Transactional
	public EnrollmentResponse updateStatus(Long enrollmentId, ProgressStatus status) {
		Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đăng ký khóa học"));

		enrollment.setStatus(status);

		// Nếu trạng thái là hoàn thành, cập nhật thời gian hoàn thành
		if (status == ProgressStatus.COMPLETED) {
			enrollment.setCompleteAt(LocalDateTime.now());
			enrollment.setProgressPercentage(100.0);
		}

		Enrollment updatedEnrollment = enrollmentRepository.save(enrollment);
		return enrollmentMapper.toDto(updatedEnrollment);
	}

	@Override
	@Transactional
	public void cancelEnrollment(Long enrollmentId) {
		Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đăng ký khóa học"));

		// Cập nhật số lượng học viên của khóa học
		Course course = enrollment.getCourse();
		course.setTotalStudent(course.getTotalStudent() - 1);
		courseRepository.save(course);

		// Xóa đăng ký
		enrollmentRepository.delete(enrollment);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<EnrollmentResponse> getCourseEnrollments(UUID courseId, ProgressStatus status, Pageable pageable) {
		Page<Enrollment> enrollmentsPage;

		if (status != null) {
			// Cần thêm phương thức này vào repository
			enrollmentsPage = enrollmentRepository.findByCourseIdAndStatus(courseId, status, pageable);
		} else {
			enrollmentsPage = enrollmentRepository.findByCourseId(courseId, pageable);
		}

		return enrollmentsPage.map(enrollmentMapper::toDto);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean isEnrollmentOwnerByUser(Long enrollmentId, UUID userId) {
		Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đăng ký khóa học"));
		return enrollment.getUser().getId().equals(userId);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean isCourseOwnerByInstructor(UUID courseId, UUID instructorId) {
		Course course = courseRepository.findById(courseId)
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học"));

		// Kiểm tra xem instructorId có nằm trong danh sách giảng viên của khóa học
		// không
		return course.getInstructors().stream().anyMatch(instructor -> instructor.getId().equals(instructorId));
	}

	@Transactional(readOnly = true)
	@Override
	public boolean isEnrollmentInCourseOfInstructor(Long enrollmentId, UUID instructorId) {
		// Tìm enrollment
		Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đăng ký khóa học"));

		// Lấy courseId từ enrollment
		UUID courseId = enrollment.getCourse().getId();

		// Kiểm tra xem instructor có phải là người dạy khóa học này không
		return isCourseOwnerByInstructor(courseId, instructorId);
	}

	@Override
	@Transactional(readOnly = true)
	public PaginationResponse<StudentProgressDto> getStudentsProgressForInstructor(UUID instructorId, UUID courseId,
			String search, ProgressStatus status, Pageable pageable) {

		// Nếu không có courseId, lấy tất cả khóa học của instructor
		List<UUID> courseIds;
		if (courseId != null) {
			// Kiểm tra xem instructor có phải là người dạy khóa học này không
			if (!isCourseOwnerByInstructor(courseId, instructorId)) {
				throw new AccessDeniedException("Bạn không có quyền xem danh sách học viên của khóa học này");
			}
			courseIds = List.of(courseId);
		} else {
			courseIds = getCourseIdsByInstructor(instructorId);
			if (courseIds.isEmpty()) {
				return PaginationResponse.<StudentProgressDto>builder().content(List.of()).totalElements(0L)
						.totalPages(0).currentPage(pageable.getPageNumber()).size(pageable.getPageSize()).build();
			}
		}

		// Tạo specification để lọc theo các điều kiện
		Specification<Enrollment> spec = Specification.where((root, query, cb) -> {
			// Lọc theo danh sách courseIds
			return root.get("course").get("id").in(courseIds);
		});

		// Thêm điều kiện lọc theo trạng thái nếu có
		if (status != null) {
			spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
		}

		// Thêm điều kiện tìm kiếm theo tên hoặc email học viên nếu có
		if (search != null && !search.trim().isEmpty()) {
			String searchLike = "%" + search.trim().toLowerCase() + "%";
			spec = spec.and((root, query, cb) -> cb.or(cb.like(cb.lower(root.get("user").get("fullName")), searchLike),
					cb.like(cb.lower(root.get("user").get("email")), searchLike)));
		}

		// Thực hiện truy vấn với specification và phân trang
		Page<Enrollment> enrollmentsPage = enrollmentRepository.findAll(spec, pageable);

		// Chuyển đổi kết quả sang DTO
		List<StudentProgressDto> progressDtos = enrollmentsPage.getContent().stream().map(studentProgressMapper::toDto)
				.collect(Collectors.toList());

		// Tạo và trả về đối tượng PaginationResponse
		return PaginationResponse.<StudentProgressDto>builder().content(progressDtos)
				.totalElements(enrollmentsPage.getTotalElements()).totalPages(enrollmentsPage.getTotalPages())
				.currentPage(enrollmentsPage.getNumber()).size(enrollmentsPage.getSize()).build();
	}

	@Override
	@Transactional(readOnly = true)
	public boolean isInstructor(UUID instructorId) {
		return courseInstructorRepository.existsByInstructorId(instructorId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<UUID> getCourseIdsByInstructor(UUID instructorId) {
		// Lấy danh sách khóa học mà instructor giảng dạy
		return courseInstructorRepository.findCourseIdsByInstructorId(instructorId);
	}
}