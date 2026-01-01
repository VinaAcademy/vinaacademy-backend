package com.vinaacademy.platform.feature.course.repository;

import com.vinaacademy.platform.feature.course.entity.Course;
import com.vinaacademy.platform.feature.course.enums.CourseLevel;
import com.vinaacademy.platform.feature.course.enums.CourseStatus;
import com.vinaacademy.platform.feature.course.projection.CourseListProjection;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID>, JpaSpecificationExecutor<Course> {

    Optional<Course> findBySlug(String slug);

    /**
     * Check if a course exists with the given slug excluding a specific course ID
     * Used for slug uniqueness validation during updates
     */
    boolean existsBySlugAndIdNot(String slug, UUID id);

    /**
     * Find course by slug with all related entities for course details page
     * Uses EntityGraph to avoid N+1 queries
     */
    @EntityGraph(attributePaths = {"category", "sections", "sections.lessons"})
    @Query("SELECT c FROM Course c WHERE c.slug = :slug")
    Optional<Course> findBySlugWithDetails(@Param("slug") String slug);

    /**
     * Find course by ID with all related entities for course details page
     * Uses EntityGraph to avoid N+1 queries
     */
    @EntityGraph(attributePaths = {"category", "sections", "sections.lessons"})
    @Query("SELECT c FROM Course c WHERE c.id = :id")
    Optional<Course> findByIdWithDetails(@Param("id") UUID id);

    /**
     * Find course by slug for learning with sections and lessons
     * Uses EntityGraph to fetch necessary data for learning
     */
    @EntityGraph(attributePaths = {"category", "sections", "sections.lessons"})
    @Query("SELECT c FROM Course c WHERE c.slug = :slug")
    Optional<Course> findBySlugForLearning(@Param("slug") String slug);

    boolean existsBySlug(String slug);

    @Query("SELECT c FROM Course c WHERE c.category.slug = :slug")
    List<Course> findAllCourseByCategory(@Param("slug") String slug);

    // Tìm tất cả khóa học theo trạng thái
    List<Course> findByStatus(CourseStatus status);

    // Tìm khóa học theo danh mục và trạng thái
    Page<Course> findByCategoryIdAndStatus(UUID categoryId, CourseStatus status, Pageable pageable);

    boolean existsById(UUID id);

    Page<Course> findAll(Pageable pageable);

    Page<Course> findByCategorySlug(String categorySlug, Pageable pageable);

    Page<Course> findByRatingGreaterThanEqual(double minRating, Pageable pageable);

    Page<Course> findByCategorySlugAndRatingGreaterThanEqual(String CategorySlug, double minRating, Pageable pageable);

    // Tìm kiếm khóa học theo tên hoặc mô tả
    @Query("SELECT c FROM Course c WHERE " + "(LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " + "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " + "AND c.status = :status")
    Page<Course> searchCourses(@Param("keyword") String keyword, @Param("status") CourseStatus status, Pageable pageable);

    // Lấy khóa học phổ biến dựa trên số lượng học viên đăng ký
    @Query("SELECT c FROM Course c WHERE c.status = :status ORDER BY c.totalStudent DESC")
    Page<Course> findPopularCourses(@Param("status") CourseStatus status, Pageable pageable);

    // Lấy khóa học mới nhất dựa trên thời gian tạo
    @Query("SELECT c FROM Course c WHERE c.status = :status ORDER BY c.createdDate DESC")
    Page<Course> findNewestCourses(@Param("status") CourseStatus status, Pageable pageable);

    // Lấy khóa học của giảng viên
    @Query("SELECT c FROM Course c JOIN c.instructors i WHERE i.instructor.id = :instructorId")
    Page<Course> findByInstructorId(@Param("instructorId") UUID instructorId, Pageable pageable);

    // Lấy khóa học mà người dùng đã đăng ký
    @Query("SELECT c FROM Course c JOIN c.enrollments e WHERE e.user.id = :userId")
    Page<Course> findEnrolledCoursesByUserId(@Param("userId") UUID userId, Pageable pageable);

    // Lấy khóa học dựa trên đánh giá cao
    @Query("SELECT c FROM Course c WHERE c.status = :status ORDER BY c.rating DESC")
    Page<Course> findTopRatedCourses(@Param("status") CourseStatus status, Pageable pageable);

    // Đếm số lượng khóa học theo trạng thái
    long countByStatus(CourseStatus status);

    // Đếm số lượng khóa học mà người dùng đã tạo
    @Query("SELECT COUNT(c) FROM Course c JOIN c.instructors i WHERE i.instructor.id = :instructorId")
    long countCoursesByInstructorId(@Param("instructorId") UUID instructorId);

    // Đếm số lượng khóa học published của một instructor
    long countByStatusAndInstructors_Instructor_Id(CourseStatus status, UUID instructorId);

    // Tìm kiếm khóa học theo nhiều tiêu chí
    @Query("SELECT c FROM Course c WHERE " + "(:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " + "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " + "AND (:status IS NULL OR c.status = :status) " + "AND (:categoryId IS NULL OR c.category.id = :categoryId) " + "AND (:level IS NULL OR c.level = :level) " + "AND (:language IS NULL OR c.language = :language) " + "AND (:minPrice IS NULL OR c.price >= :minPrice) " + "AND (:maxPrice IS NULL OR c.price <= :maxPrice) " + "AND (:minRating IS NULL OR c.rating >= :minRating)")
    Page<Course> advancedSearchCourses(@Param("keyword") String keyword, @Param("status") CourseStatus status, @Param("categoryId") UUID categoryId, @Param("level") CourseLevel level, @Param("language") String language, @Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice, @Param("minRating") Double minRating, Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " + "FROM Course c JOIN c.enrollments e JOIN c.sections s " + "JOIN s.lessons ls" + " WHERE e.user.id = :studentId AND ls.id = :lessonId")
    boolean existsByStudentAndLesson(UUID studentId, UUID lessonId);

    @Query("SELECT c FROM Course c JOIN c.sections s " + "WHERE s.id = :sectionId")
    Optional<Course> getCourseBySectionId(UUID sectionId);

    @Query("SELECT c.status, COUNT(c) FROM Course c GROUP BY c.status")
    List<Object[]> countCoursesByStatus();

    /**
     * Get course count grouped by category
     * Returns: [categoryId, categoryName, categorySlug, courseCount]
     */
    @Query("SELECT c.category.id, c.category.name, c.category.slug, COUNT(c) " +
           "FROM Course c " +
           "WHERE c.status = 'PUBLISHED' " +
           "GROUP BY c.category.id, c.category.name, c.category.slug " +
           "ORDER BY COUNT(c) DESC")
    List<Object[]> countCoursesByCategory();

    /**
     * Count courses created in a specific date range
     */
    @Query("SELECT COUNT(c) FROM Course c WHERE c.createdDate >= :startDate AND c.createdDate < :endDate")
    long countCoursesByDateRange(@Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * Count courses by status in a specific date range
     */
    @Query("SELECT COUNT(c) FROM Course c WHERE c.status = :status AND c.createdDate >= :startDate AND c.createdDate < :endDate")
    long countByStatusAndDateRange(@Param("status") CourseStatus status, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * Lightweight projection query for course pagination/search
     * Only selects essential fields for better performance
     */
    @Query("""
            SELECT c.id as id,
                   c.name as name,
                   c.slug as slug,
                   c.description as description,
                   c.image as image,
                   c.level as level,
                   c.language as language,
                   c.price as price,
                   c.rating as rating,
                   c.totalStudent as totalStudent,
                   c.totalLesson as totalLesson,
                   c.totalSection as totalSection,
                   c.status as status,
                   c.createdDate as createdDate,
                   c.updatedDate as updatedDate,
                   c.category.name as categoryName,
                   c.category.slug as categorySlug,
                   COUNT(ci.instructor) as instructorCount
            FROM Course c 
            LEFT JOIN c.category cat
            LEFT JOIN c.instructors ci
            WHERE c.status = :status
            GROUP BY c.id, c.name, c.slug, c.description, c.image, c.level, 
                     c.language, c.price, c.rating, c.totalStudent, c.totalLesson, 
                     c.totalSection, c.status, c.createdDate, c.updatedDate,
                     c.category.name, c.category.slug
            """)
    Page<CourseListProjection> findCourseProjectionsByStatus(@Param("status") CourseStatus status, Pageable pageable);

    /**
     * Lightweight projection query for instructor courses
     */
    @Query("""
            SELECT c.id as id,
                   c.name as name,
                   c.slug as slug,
                   c.description as description,
                   c.image as image,
                   c.level as level,
                   c.language as language,
                   c.price as price,
                   c.rating as rating,
                   c.totalStudent as totalStudent,
                   c.totalLesson as totalLesson,
                   c.totalSection as totalSection,
                   c.status as status,
                   c.createdDate as createdDate,
                   c.updatedDate as updatedDate,
                   c.category.name as categoryName,
                   c.category.slug as categorySlug,
                   COUNT(ci.instructor) as instructorCount
            FROM Course c 
            LEFT JOIN c.category cat
            LEFT JOIN c.instructors ci
            WHERE ci.instructor.id = :instructorId
            GROUP BY c.id, c.name, c.slug, c.description, c.image, c.level, 
                     c.language, c.price, c.rating, c.totalStudent, c.totalLesson, 
                     c.totalSection, c.status, c.createdDate, c.updatedDate,
                     c.category.name, c.category.slug
            """)
    Page<CourseListProjection> findCourseProjectionsByInstructor(@Param("instructorId") UUID instructorId, Pageable pageable);

    /**
     * Get monthly trend data for courses created in the last N months
     * Returns [year, month, count] for courses created
     */
    @Query("""
            SELECT YEAR(c.createdDate) as year,
                   MONTH(c.createdDate) as month,
                   COUNT(c) as count
            FROM Course c
            WHERE c.createdDate >= :startDate
            GROUP BY YEAR(c.createdDate), MONTH(c.createdDate)
            ORDER BY YEAR(c.createdDate), MONTH(c.createdDate)
            """)
    List<Object[]> countCoursesCreatedByMonth(@Param("startDate") java.time.LocalDateTime startDate);

    /**
     * Get monthly trend data for courses published in the last N months
     * Returns [year, month, count] for courses published
     * Uses updatedDate as published date approximation since courses are updated when status changes to PUBLISHED
     */
    @Query("""
            SELECT YEAR(c.updatedDate) as year,
                   MONTH(c.updatedDate) as month,
                   COUNT(c) as count
            FROM Course c
            WHERE c.updatedDate >= :startDate
            AND c.status = :status
            GROUP BY YEAR(c.updatedDate), MONTH(c.updatedDate)
            ORDER BY YEAR(c.updatedDate), MONTH(c.updatedDate)
            """)
    List<Object[]> countCoursesPublishedByMonth(@Param("startDate") java.time.LocalDateTime startDate, @Param("status") CourseStatus status);

    /**
     * Get top N courses ordered by multiple performance metrics
     * Returns courses with highest combination of students, revenue, and rating
     */
    @Query("""
            SELECT c
            FROM Course c
            WHERE c.status = :status
            ORDER BY c.totalStudent DESC, c.rating DESC
            """)
    List<Course> findTopCourses(@Param("status") CourseStatus status, Pageable pageable);

    /**
     * Get recently published courses
     * Returns courses ordered by updatedDate descending (most recent first)
     */
    @Query("""
            SELECT c
            FROM Course c
            WHERE c.status = :status
            ORDER BY c.updatedDate DESC
            """)
    List<Course> findRecentPublishedCourses(@Param("status") CourseStatus status, Pageable pageable);

    /**
     * Get published courses for approval time calculation
     * Returns courses to calculate average approval time in Java
     */
    @Query("""
            SELECT c
            FROM Course c
            WHERE c.status = :status
            AND c.createdDate IS NOT NULL
            AND c.updatedDate IS NOT NULL
            """)
    List<Course> findCoursesForApprovalTimeCalculation(@Param("status") CourseStatus status);

    /**
     * Count pending courses (for alerts)
     */
    @Query("""
            SELECT COUNT(c)
            FROM Course c
            WHERE c.status = :status
            """)
    long countPendingCourses(@Param("status") CourseStatus status);

    /**
     * Find pending courses to check for overdue status
     */
    @Query("""
            SELECT c
            FROM Course c
            WHERE c.status = :status
            AND c.createdDate IS NOT NULL
            """)
    List<Course> findPendingCoursesForOverdueCheck(@Param("status") CourseStatus status);

    /**
     * Find published courses with no students to check for inactive status
     */
    @Query("""
            SELECT c
            FROM Course c
            WHERE c.status = :status
            AND c.totalStudent = 0
            AND c.updatedDate IS NOT NULL
            """)
    List<Course> findPublishedCoursesForInactiveCheck(@Param("status") CourseStatus status);

    /**
     * Count low-rated courses (rating below threshold with at least 1 rating)
     */
    @Query("""
            SELECT COUNT(c)
            FROM Course c
            WHERE c.status = :status
            AND c.totalRating > 0
            AND c.rating < :threshold
            """)
    long countLowRatedCourses(@Param("status") CourseStatus status, @Param("threshold") double threshold);

    // ==================== Admin Dashboard Queries ====================
    
    /**
     * Đếm courses created sau một thời điểm
     * Dùng cho dashboard stats
     */
    @Query("SELECT COUNT(c) FROM Course c WHERE c.createdDate >= :startDate")
    Long countByCreatedDateAfter(@Param("startDate") java.time.LocalDateTime startDate);
    
    /**
     * Đếm courses theo status created sau một thời điểm
     */
    @Query("SELECT COUNT(c) FROM Course c " +
           "WHERE c.status = :status AND c.createdDate >= :startDate")
    Long countByStatusAndCreatedDateAfter(@Param("status") CourseStatus status,
                                          @Param("startDate") java.time.LocalDateTime startDate);
    
    /**
     * Đếm courses created trong khoảng thời gian
     */
    @Query("SELECT COUNT(c) FROM Course c " +
           "WHERE c.createdDate >= :startDate AND c.createdDate < :endDate")
    Long countByCreatedDateBetween(@Param("startDate") java.time.LocalDateTime startDate,
                                   @Param("endDate") java.time.LocalDateTime endDate);
    
    /**
     * Lấy monthly course creation trend
     * Returns: [year-month, created_count, published_count]
     */
    @Query("SELECT TO_CHAR(c.createdDate, 'YYYY-MM') as month, " +
           "COUNT(c) as courseCount, " +
           "SUM(CASE WHEN c.status = 'PUBLISHED' THEN 1 ELSE 0 END) as publishedCount " +
           "FROM Course c " +
           "WHERE c.createdDate >= :startDate " +
           "GROUP BY TO_CHAR(c.createdDate, 'YYYY-MM') " +
           "ORDER BY TO_CHAR(c.createdDate, 'YYYY-MM')")
    List<Object[]> getMonthlyCourseTrend(@Param("startDate") java.time.LocalDateTime startDate);
    
    /**
     * Lấy top courses theo số lượng students
     * Returns courses với enrollment count
     */
    @Query("SELECT c FROM Course c " +
           "WHERE c.status = 'PUBLISHED' " +
           "ORDER BY c.totalStudent DESC")
    Page<Course> findTopCoursesByStudents(Pageable pageable);
    
    /**
     * Lấy recent published courses
     */
    @Query("SELECT c FROM Course c " +
           "WHERE c.status = 'PUBLISHED' " +
           "ORDER BY c.createdDate DESC")
    Page<Course> findRecentPublishedCourses(Pageable pageable);
    
    /**
     * Lấy recent courses (bất kể status)
     */
    @Query("SELECT c FROM Course c ORDER BY c.createdDate DESC")
    Page<Course> findRecentCourses(Pageable pageable);

}

