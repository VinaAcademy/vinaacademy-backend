package com.vinaacademy.platform.feature.section.repository;

import com.vinaacademy.platform.feature.course.entity.Course;
import com.vinaacademy.platform.feature.section.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SectionRepository extends JpaRepository<Section, UUID> {
    /**
     * Find sections by course ordered by their index
     */
    List<Section> findByCourseOrderByOrderIndex(Course course);
    
    /**
     * Check if a section with the given title exists in a course
     */
    boolean existsByTitleAndCourse(String title, Course course);
}
