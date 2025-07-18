package com.vinaacademy.platform.feature.course.repository;

import com.vinaacademy.platform.feature.lesson.entity.Lesson;
import com.vinaacademy.platform.feature.lesson.entity.UserProgress;
import com.vinaacademy.platform.feature.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {
    Optional<UserProgress> findByLessonAndUser(Lesson lesson, User currentUser);

    @Query("SELECT up FROM UserProgress up WHERE up.user = :user AND up.lesson IN :lessons")
    List<UserProgress> findByUserAndLessonIn(@Param("user") User user, @Param("lessons") List<Lesson> lessons);

    Optional<UserProgress> findByLessonIdAndUserId(UUID lessonId, UUID userId);

    UUID user(User user);

    @Query("SELECT COUNT(up) FROM UserProgress up " +
            "WHERE up.user.id = :userId AND up.lesson.section.course.id = :courseId " +
            "AND up.completed = true")
    long countCompletedByUserIdAndCourseId(UUID userId, UUID courseId);

    @Query("SELECT up FROM UserProgress up " +
            "WHERE up.user.id = :userId AND up.lesson.section.course.id = :courseId")
    List<UserProgress> findLessonProgressByCourseUser(UUID courseId, UUID userId);
}
