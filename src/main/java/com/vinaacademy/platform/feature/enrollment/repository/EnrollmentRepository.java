package com.vinaacademy.platform.feature.enrollment.repository;

import com.vinaacademy.platform.feature.course.entity.Course;
import com.vinaacademy.platform.feature.enrollment.Enrollment;
import com.vinaacademy.platform.feature.enrollment.enums.ProgressStatus;
import com.vinaacademy.platform.feature.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long>, JpaSpecificationExecutor<Enrollment>  {
    //Tìm đăng ký khóa học theo người dùng và khóa học
    Optional<Enrollment> findByUserIdAndCourseId(UUID userId, UUID courseId);

    //Kiểm tra người dùng đã đăng ký khóa học chưa
    boolean existsByUserIdAndCourseId(UUID userId, UUID courseId);

    //Lấy tất cả đăng ký khóa học của một người dùng
    List<Enrollment> findByUserId(UUID userId);

    //Lấy tất cả đăng ký khóa học của một người dùng (có phân trang)
    Page<Enrollment> findByUserId(UUID userId, Pageable pageable);

    //Lấy tất cả đăng ký khóa học của một khóa học
    List<Enrollment> findByCourseId(UUID courseId);

        //Lấy tất cả đăng ký khóa học của một người dùng theo trạng thái
    List<Enrollment> findByUserIdAndStatus(UUID userId, ProgressStatus status);

    //Lấy tất cả đăng ký khóa học của một người dùng theo trạng thái (có phân trang)
    Page<Enrollment> findByUserIdAndStatus(UUID userId, ProgressStatus status, Pageable pageable);

    //Đếm số lượng đăng ký khóa học của một khóa học
    long countByCourseId(UUID courseId);

    //Đếm số lượng đăng ký khóa học của một người dùng
    long countByUserId(UUID userId);

    //Lấy những đăng ký khóa học mới trong khoảng thời gian
    List<Enrollment> findByStartAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    //Tìm kiếm các khóa học đã hoàn thành
    List<Enrollment> findByStatusAndCompleteAtIsNotNull(ProgressStatus status);

    //Thống kê số lượng đăng ký khóa học theo ngày
    @Query("SELECT DATE(e.startAt), COUNT(e) FROM Enrollment e " +
            "WHERE e.startAt BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(e.startAt) ORDER BY DATE(e.startAt)")
    List<Object[]> countEnrollmentsByDay(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    //Tìm những đăng ký khóa học có tiến độ cao hơn một giá trị cụ thể
    List<Enrollment> findByProgressPercentageGreaterThanEqual(Double percentage);

    //Tìm những đăng ký khóa học có tiến độ thấp hơn một giá trị cụ thể
    List<Enrollment> findByProgressPercentageLessThan(Double percentage);

    //Cập nhật trạng thái cho tất cả đăng ký của một khóa học
    @Modifying
    @Query("UPDATE Enrollment e SET e.status = :status WHERE e.course.id = :courseId")
    void updateStatusByCourseId(@Param("courseId") UUID courseId, @Param("status") ProgressStatus status);

    //Tìm các đăng ký khóa học đang hoạt động (chưa hoàn thành và đã bắt đầu)
    @Query("SELECT e FROM Enrollment e WHERE e.user.id = :userId AND e.status = 'IN_PROGRESS'")
    List<Enrollment> findActiveEnrollmentsByUserId(@Param("userId") UUID userId);


    //Lấy tất cả đăng ký khóa học của một khóa học (có phân trang)
    Page<Enrollment> findByCourseId(UUID courseId, Pageable pageable);

    //Lấy tất cả đăng ký khóa học của một khóa học theo trạng thái (có phân trang)
    Page<Enrollment> findByCourseIdAndStatus(UUID courseId, ProgressStatus status, Pageable pageable);
    
    Optional<Enrollment> findByCourseAndUser(Course course, User currentUser);

    boolean existsByCourseIdAndUserId(UUID courseId, UUID studentId);
    
    Long countByUserAndStatus(User user, ProgressStatus status);
    
    Long countByUser(User user);
}
