package com.vinaacademy.platform.feature.video.repository;

import com.vinaacademy.platform.feature.video.entity.Video;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VideoRepository extends JpaRepository<Video, UUID> {
    @Query("SELECT v FROM Video v JOIN v.section s JOIN s.course c WHERE c.id = :courseId")
    List<Video> findByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT v FROM Video v JOIN v.section s JOIN s.course c " +
            "JOIN c.enrollments e WHERE e.user.id = :userId")
    List<Video> findByEnrolledUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(e) > 0 FROM Lesson l " +
            "JOIN l.section s JOIN s.course c " +
            "JOIN c.enrollments e WHERE l.id = :lessonId AND e.user.id = :userId")
    boolean isUserEnrolledInCourse(@Param("lessonId") UUID lessonId, @Param("userId") UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM Video v WHERE v.id = :videoId")
    Optional<Video> findByIdWithLock(UUID videoId);
}
