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
    
    @Query(nativeQuery = true, value = "SELECT user_id FROM enrollments WHERE course_id = :courseId")
    List<UUID> findUserIdsByCourseId(@Param("courseId") UUID courseId);

    //Lấy tất cả đăng ký khóa học của nhiều khóa học
    List<Enrollment> findByCourseIdIn(List<UUID> courseIds);

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

    /**
     * Đếm số lượng enrollments theo danh sách courseIds trong khoảng thời gian
     */
    @Query("SELECT COUNT(e) FROM Enrollment e " +
           "WHERE e.course.id IN :courseIds " +
           "AND e.startAt >= :startDate " +
           "AND e.startAt <= :endDate")
    Long countByCourseIdsAndDateRange(
            @Param("courseIds") List<UUID> courseIds,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Đếm tổng số enrollments theo danh sách courseIds
     */
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.id IN :courseIds")
    Long countByCourseIds(@Param("courseIds") List<UUID> courseIds);

    /**
     * Đếm số enrollments đã hoàn thành theo danh sách courseIds
     */
    @Query("SELECT COUNT(e) FROM Enrollment e " +
           "WHERE e.course.id IN :courseIds " +
           "AND e.status = :status")
    Long countByCourseIdsAndStatus(
            @Param("courseIds") List<UUID> courseIds,
            @Param("status") ProgressStatus status
    );

    /**
     * Đếm số enrollments đã hoàn thành cho một course cụ thể
     */
    @Query("SELECT COUNT(e) FROM Enrollment e " +
           "WHERE e.course.id = :courseId " +
           "AND e.status = :status")
    Long countByCourseIdAndStatus(
            @Param("courseId") UUID courseId,
            @Param("status") ProgressStatus status
    );

    /**
     * Lấy danh sách enrollments gần đây cho các courses của giảng viên
     */
    @Query("SELECT e FROM Enrollment e " +
           "WHERE e.course.id IN :courseIds " +
           "ORDER BY e.startAt DESC")
    Page<Enrollment> findRecentEnrollmentsByCourseIds(
            @Param("courseIds") List<UUID> courseIds,
            Pageable pageable
    );

    /**
     * Lấy tổng số enrollments cho instructor (tất cả courses)
     */
    @Query("SELECT COUNT(DISTINCT e.user.id) FROM Enrollment e WHERE e.course.id IN :courseIds")
    Long countDistinctUsersByCourseIds(@Param("courseIds") List<UUID> courseIds);

    /**
     * Lấy average completion rate cho instructor's courses
     */
    @Query("SELECT AVG(e.progressPercentage) FROM Enrollment e WHERE e.course.id IN :courseIds")
    Double getAverageProgressByCourseIds(@Param("courseIds") List<UUID> courseIds);

    /**
     * Lấy top 5 courses theo số lượng students
     */
    @Query(value = "SELECT c.id, c.name, c.image, COUNT(DISTINCT e.user_id) " +
           "FROM enrollments e " +
           "JOIN courses c ON e.course_id = c.id " +
           "WHERE c.id IN :courseIds " +
           "GROUP BY c.id, c.name, c.image " +
           "ORDER BY COUNT(DISTINCT e.user_id) DESC " +
           "LIMIT :limit", nativeQuery = true)
    List<Object[]> getTopCoursesByStudentCount(@Param("courseIds") List<UUID> courseIds, @Param("limit") int limit);

    /**
     * Lấy danh sách users với enrollments trong courses của instructor
     */
    @Query(value = "SELECT DISTINCT e.user_id, CAST(u.full_name AS TEXT), CAST(u.email AS TEXT), " +
           "COUNT(e.id), AVG(e.progress_percentage), MAX(e.start_at) " +
           "FROM enrollments e " +
           "JOIN users u ON u.id = e.user_id " +
           "WHERE e.course_id IN :courseIds " +
           "AND (CAST(:minProgress AS NUMERIC) IS NULL OR e.progress_percentage >= CAST(:minProgress AS NUMERIC)) " +
           "AND (CAST(:maxProgress AS NUMERIC) IS NULL OR e.progress_percentage <= CAST(:maxProgress AS NUMERIC)) " +
           "AND (CAST(:keyword AS TEXT) IS NULL " +
           "     OR CAST(u.full_name AS TEXT) ILIKE '%' || CAST(:keyword AS TEXT) || '%' " +
           "     OR CAST(u.email AS TEXT) ILIKE '%' || CAST(:keyword AS TEXT) || '%') " +
           "GROUP BY e.user_id, CAST(u.full_name AS TEXT), CAST(u.email AS TEXT)",
           nativeQuery = true)
    Page<Object[]> findStudentsSummary(
            @Param("courseIds") List<UUID> courseIds,
            @Param("minProgress") Double minProgress,
            @Param("maxProgress") Double maxProgress,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /**
     * Lấy enrollments của một user trong các courses của instructor
     */
    @Query("SELECT e FROM Enrollment e WHERE e.user.id = :userId AND e.course.id IN :courseIds")
    List<Enrollment> findByUserIdAndCourseIds(@Param("userId") UUID userId, @Param("courseIds") List<UUID> courseIds);
}

