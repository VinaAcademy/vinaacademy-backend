package com.vinaacademy.platform.feature.lesson.repository;

import com.vinaacademy.platform.feature.lesson.entity.Lesson;
import com.vinaacademy.platform.feature.lesson.repository.projection.LessonAccessInfoDto;
import com.vinaacademy.platform.feature.section.entity.Section;
import com.vinaacademy.platform.feature.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, UUID> {
    /**
     * Find lessons by section ordered by their index
     */
    List<Lesson> findBySectionOrderByOrderIndex(Section section);

    /**
     * Find lessons by author
     */
    List<Lesson> findByAuthor(User author);

    /**
     * Find lessons by section and title
     */
    Optional<Lesson> findBySectionAndTitle(Section section, String title);

    /**
     * Check if a lesson with the given title exists in a section
     */
    boolean existsBySectionAndTitle(Section section, String title);

    /**
     * Count lessons in a section
     */
    long countBySection(Section section);

    /**
     * Find free lessons in a course
     */
    @Query("SELECT l FROM Lesson l WHERE l.section.course.id = :courseId AND l.free = true ORDER BY l.section.orderIndex, l.orderIndex")
    List<Lesson> findFreeLessonsByCourseId(@Param("courseId") Long courseId);

    /**
     * Find all lessons in a course ordered by section and lesson order
     */
    @Query("SELECT l FROM Lesson l WHERE l.section.course.id = :courseId ORDER BY l.section.orderIndex, l.orderIndex")
    List<Lesson> findAllByCourseIdOrdered(@Param("courseId") Long courseId);

    @Query("""
            SELECT new com.vinaacademy.platform.feature.lesson.repository.projection.LessonAccessInfoDto(
                l.id,
                l.title,
                l.free,
                CASE WHEN EXISTS (
                            SELECT 1 FROM CourseInstructor ci
                                        WHERE ci.instructor.id = :userId
                                                    AND ci.course = s.course
                ) THEN true ELSE false END,
                CASE WHEN EXISTS (
                            SELECT 1 FROM Enrollment en
                                        WHERE en.user.id = :userId
                                                    AND en.course = s.course
                ) THEN true ELSE false END
            )
            FROM Lesson l JOIN l.section s
            WHERE l.id = :lessonId""")
    Optional<LessonAccessInfoDto> getLessonAccessInfo(
            @Param("lessonId") UUID lessonId,
            @Param("userId") UUID userId
    );

    long countBySectionCourseId(UUID id);


}
