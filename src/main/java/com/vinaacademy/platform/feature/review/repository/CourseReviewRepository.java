package com.vinaacademy.platform.feature.review.repository;
import com.vinaacademy.platform.feature.review.entity.CourseReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseReviewRepository extends JpaRepository<CourseReview, Long> {
    @Query("SELECT cr FROM CourseReview cr WHERE cr.course.id = :courseId " +
           "AND (cr.isHidden = false OR cr.isHidden IS NULL) " +
           "AND (cr.isDeleted = false OR cr.isDeleted IS NULL)")
    Page<CourseReview> findByCourseId(@Param("courseId") UUID courseId, Pageable pageable);

    @Query("SELECT cr FROM CourseReview cr WHERE cr.user.id = :userId " +
           "AND (cr.isHidden = false OR cr.isHidden IS NULL) " +
           "AND (cr.isDeleted = false OR cr.isDeleted IS NULL)")
    List<CourseReview> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT cr FROM CourseReview cr WHERE cr.course.id = :courseId " +
           "AND cr.user.id = :userId " +
           "AND (cr.isDeleted = false OR cr.isDeleted IS NULL)")
    Optional<CourseReview> findByCourseIdAndUserId(@Param("courseId") UUID courseId, @Param("userId") UUID userId);

    @Query("SELECT CASE WHEN COUNT(cr) > 0 THEN true ELSE false END FROM CourseReview cr " +
           "WHERE cr.course.id = :courseId AND cr.user.id = :userId " +
           "AND (cr.isDeleted = false OR cr.isDeleted IS NULL)")
    boolean existsByCourseIdAndUserId(@Param("courseId") UUID courseId, @Param("userId") UUID userId);

    Optional<CourseReview> findByIdAndUserId(Long id, UUID userId);

    @Query("SELECT COUNT(cr) FROM CourseReview cr WHERE cr.course.id = :courseId " +
           "AND (cr.isHidden = false OR cr.isHidden IS NULL) " +
           "AND (cr.isDeleted = false OR cr.isDeleted IS NULL)")
    Long countByCourseId(@Param("courseId") UUID courseId);

    @Query("SELECT AVG(cr.rating) FROM CourseReview cr WHERE cr.course.id = :courseId " +
           "AND (cr.isHidden = false OR cr.isHidden IS NULL) " +
           "AND (cr.isDeleted = false OR cr.isDeleted IS NULL)")
    Double calculateAverageRatingByCourseId(@Param("courseId") UUID courseId);

    @Query("SELECT cr.rating as rating, COUNT(cr) as count FROM CourseReview cr " +
            "WHERE cr.course.id = :courseId " +
            "AND (cr.isHidden = false OR cr.isHidden IS NULL) " +
            "AND (cr.isDeleted = false OR cr.isDeleted IS NULL) " +
            "GROUP BY cr.rating ORDER BY cr.rating")
    List<Object[]> countRatingsByCourseId(@Param("courseId") UUID courseId);

    @Query("SELECT CASE WHEN COUNT(cr) > 0 THEN true ELSE false END FROM CourseReview cr " +
           "WHERE cr.id = :id AND cr.user.id = :userId " +
           "AND (cr.isDeleted = false OR cr.isDeleted IS NULL)")
    boolean existsByIdAndUserId(@Param("id") Long id, @Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE CourseReview cr SET cr.rating = :rating, cr.review = :review, " +
           "cr.updatedDate = :updatedDate WHERE cr.id = :id")
    int updateReview(@Param("id") Long id, 
                    @Param("rating") Integer rating, 
                    @Param("review") String review, 
                    @Param("updatedDate") LocalDateTime updatedDate);

    /**
     * Lấy danh sách reviews gần đây cho các courses của giảng viên
     */
    @Query("SELECT cr FROM CourseReview cr " +
           "WHERE cr.course.id IN :courseIds " +
           "AND (cr.isHidden = false OR cr.isHidden IS NULL) " +
           "AND (cr.isDeleted = false OR cr.isDeleted IS NULL) " +
           "ORDER BY cr.createdDate DESC")
    Page<CourseReview> findRecentReviewsByCourseIds(
            @Param("courseIds") List<UUID> courseIds,
            Pageable pageable
    );

    /**
     * Lấy danh sách reviews bị ẩn (cho admin)
     */
    @Query("SELECT cr FROM CourseReview cr WHERE cr.isHidden = true " +
           "ORDER BY cr.hiddenAt DESC")
    Page<CourseReview> findHiddenReviews(Pageable pageable);

    /**
     * Lấy review bị ẩn theo ID (cho admin)
     */
    @Query("SELECT cr FROM CourseReview cr WHERE cr.id = :id AND cr.isHidden = true")
    Optional<CourseReview> findHiddenReviewById(@Param("id") Long id);

    /**
     * Cập nhật trạng thái ẩn review
     */
    @Modifying
    @Query("UPDATE CourseReview cr SET cr.isHidden = :isHidden, " +
           "cr.hiddenAt = :hiddenAt, cr.hiddenReason = :reason, " +
           "cr.hiddenBy = :hiddenBy WHERE cr.id = :id")
    int updateHiddenStatus(@Param("id") Long id,
                          @Param("isHidden") Boolean isHidden,
                          @Param("hiddenAt") java.time.LocalDateTime hiddenAt,
                          @Param("reason") String reason,
                          @Param("hiddenBy") UUID hiddenBy);

    // ==================== Admin Dashboard Queries ====================
    
    /**
     * Lấy recent reviews (không bị ẩn)
     * Dùng cho dashboard recent activities
     */
    @Query("SELECT cr FROM CourseReview cr " +
           "WHERE (cr.isHidden = false OR cr.isHidden IS NULL) " +
           "AND (cr.isDeleted = false OR cr.isDeleted IS NULL) " +
           "ORDER BY cr.createdDate DESC")
    Page<CourseReview> findRecentReviews(Pageable pageable);
    
    /**
     * Đếm reviews created sau một thời điểm
     */
    @Query("SELECT COUNT(cr) FROM CourseReview cr " +
           "WHERE cr.createdDate >= :startDate " +
           "AND (cr.isHidden = false OR cr.isHidden IS NULL) " +
           "AND (cr.isDeleted = false OR cr.isDeleted IS NULL)")
    Long countReviewsSince(@Param("startDate") LocalDateTime startDate);

    /**
     * Mark review as deleted (for confirmed violations)
     */
    @Modifying
    @Query("UPDATE CourseReview cr SET cr.isDeleted = true, " +
           "cr.deletedAt = :deletedAt, cr.deletedBy = :deletedBy WHERE cr.id = :id")
    int markAsDeleted(@Param("id") Long id,
                      @Param("deletedAt") LocalDateTime deletedAt,
                      @Param("deletedBy") UUID deletedBy);

}

