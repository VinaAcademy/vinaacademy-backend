package com.vinaacademy.platform.feature.course.repository;

import com.vinaacademy.platform.feature.course.entity.Course;
import com.vinaacademy.platform.feature.course.enums.CourseLevel;
import com.vinaacademy.platform.feature.course.enums.CourseStatus;
import com.vinaacademy.platform.feature.course.projection.CourseListProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    @EntityGraph(attributePaths = {"category", "instructors", "instructors.instructor", "sections", "sections.lessons", "courseReviews", "courseReviews.user"})
    @Query("SELECT c FROM Course c WHERE c.id = :id")
    Optional<Course> findByIdWithDetails(@Param("id") UUID id);

    /**
     * Find course by slug for learning with sections and lessons
     * Uses EntityGraph to fetch necessary data for learning
     */
    @EntityGraph(attributePaths = {"category", "instructors", "instructors.instructor", "sections", "sections.lessons"})
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

}
