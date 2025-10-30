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
    Page<CourseReview> findByCourseId(UUID courseId, Pageable pageable);

    List<CourseReview> findByUserId(UUID userId);

    Optional<CourseReview> findByCourseIdAndUserId(UUID courseId, UUID userId);

    boolean existsByCourseIdAndUserId(UUID courseId, UUID userId);

    Optional<CourseReview> findByIdAndUserId(Long id, UUID userId);

    @Query("SELECT AVG(cr.rating) FROM CourseReview cr WHERE cr.course.id = :courseId")
    Double calculateAverageRatingByCourseId(@Param("courseId") UUID courseId);

    @Query("SELECT cr.rating as rating, COUNT(cr) as count FROM CourseReview cr " +
            "WHERE cr.course.id = :courseId GROUP BY cr.rating ORDER BY cr.rating")
    List<Object[]> countRatingsByCourseId(@Param("courseId") UUID courseId);

    boolean existsByIdAndUserId(Long id, UUID userId);

    @Modifying
    @Query("UPDATE CourseReview cr SET cr.rating = :rating, cr.review = :review, " +
           "cr.updatedDate = :updatedDate WHERE cr.id = :id")
    int updateReview(@Param("id") Long id, 
                    @Param("rating") Integer rating, 
                    @Param("review") String review, 
                    @Param("updatedDate") LocalDateTime updatedDate);

}
