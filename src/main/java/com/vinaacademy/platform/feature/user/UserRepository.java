package com.vinaacademy.platform.feature.user;

import com.vinaacademy.platform.feature.user.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    Optional<User> findByUsername(@Param("username") String username);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    Optional<User> findByUsernameWithRoles(@Param("username") String username);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles WHERE u.email = :email")
    Optional<User> findByEmailWithRoles(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);
    
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.enrollments WHERE u.id = :userId")
    Optional<User> findByIdWithEnrollments(@Param("userId") UUID userId);
    
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.courseReviews WHERE u.id = :userId")
    Optional<User> findByIdWithCourseReviews(@Param("userId") UUID userId);
    
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.coursesTaught WHERE u.id = :userId")
    Optional<User> findByIdWithCoursesTaught(@Param("userId") UUID userId);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
    
    boolean existsByPhone(String phone);

    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> searchUsersByKeyword(String keyword, Pageable pageable);
    
    // Admin user management queries
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.code = :roleCode")
    long countByRolesCode(@Param("roleCode") String roleCode);
    
    long countByEnabled(boolean enabled);
    
    long countByLockTimeIsNotNull();
    
    long countByCreatedDateAfter(LocalDateTime dateTime);
    
    long countByIsCollaborator(boolean isCollaborator);
    
    // Count user's enrollments
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.user.id = :userId")
    long countEnrollmentsByUserId(@Param("userId") UUID userId);
    
    // Count user's created courses (as instructor)
    @Query("SELECT COUNT(ci) FROM CourseInstructor ci WHERE ci.instructor.id = :userId")
    long countCreatedCoursesByUserId(@Param("userId") UUID userId);
    
    // Count user's completed courses
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.user.id = :userId AND e.status = 'COMPLETED'")
    long countCompletedCoursesByUserId(@Param("userId") UUID userId);
    
    // ==================== Admin Dashboard Queries ====================
    
    /**
     * Đếm số users theo role và created sau một thời điểm
     * Dùng cho dashboard stats với time range
     */
    @Query("SELECT COUNT(DISTINCT u) FROM User u JOIN u.roles r " +
           "WHERE r.code = :roleCode AND u.createdDate >= :startDate")
    Long countByRoleAndCreatedDateAfter(@Param("roleCode") String roleCode, 
                                        @Param("startDate") LocalDateTime startDate);
    
    /**
     * Đếm số users created trong khoảng thời gian
     * Dùng để so sánh với kỳ trước
     */
    @Query("SELECT COUNT(u) FROM User u " +
           "WHERE u.createdDate >= :startDate AND u.createdDate < :endDate")
    Long countByCreatedDateBetween(@Param("startDate") LocalDateTime startDate, 
                                   @Param("endDate") LocalDateTime endDate);
    
    /**
     * Đếm số users theo role created trong khoảng thời gian
     */
    @Query("SELECT COUNT(DISTINCT u) FROM User u JOIN u.roles r " +
           "WHERE r.code = :roleCode " +
           "AND u.createdDate >= :startDate AND u.createdDate < :endDate")
    Long countByRoleAndCreatedDateBetween(@Param("roleCode") String roleCode,
                                         @Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);
    
    /**
     * Đếm active users (có enrollment sau một thời điểm)
     * Dùng cho retention rate calculation
     */
    @Query("SELECT COUNT(DISTINCT e.user.id) FROM Enrollment e " +
           "WHERE e.startAt >= :startDate")
    Long countActiveUsersSince(@Param("startDate") LocalDateTime startDate);
    
    /**
     * Lấy monthly user statistics cho 12 tháng
     * Returns: [year-month, count]
     */
    @Query("SELECT TO_CHAR(u.createdDate, 'YYYY-MM') as month, COUNT(u) " +
           "FROM User u " +
           "WHERE u.createdDate >= :startDate " +
           "GROUP BY TO_CHAR(u.createdDate, 'YYYY-MM') " +
           "ORDER BY TO_CHAR(u.createdDate, 'YYYY-MM')")
    List<Object[]> getMonthlyUserStats(@Param("startDate") LocalDateTime startDate);
    
    /**
     * Đếm users theo role
     * Dùng cho phân tích student/instructor percentage
     */
    @Query("SELECT COUNT(DISTINCT u) FROM User u JOIN u.roles r WHERE r.code = :roleCode")
    Long countByRole(@Param("roleCode") String roleCode);
    
    /**
     * Lấy recent instructors (giảng viên mới)
     * Chỉ lấy users có role INSTRUCTOR
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r " +
           "WHERE r.code = 'INSTRUCTOR' " +
           "ORDER BY u.createdDate DESC")
    Page<User> findRecentInstructors(Pageable pageable);
}
