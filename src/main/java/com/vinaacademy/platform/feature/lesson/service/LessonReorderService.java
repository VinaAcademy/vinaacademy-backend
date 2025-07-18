package com.vinaacademy.platform.feature.lesson.service;

import com.vinaacademy.platform.exception.BadRequestException;
import com.vinaacademy.platform.exception.NotFoundException;
import com.vinaacademy.platform.feature.lesson.entity.Lesson;
import com.vinaacademy.platform.feature.lesson.repository.LessonRepository;
import com.vinaacademy.platform.feature.section.entity.Section;
import com.vinaacademy.platform.feature.section.repository.SectionRepository;
import com.vinaacademy.platform.feature.user.auth.annotation.RequiresResourcePermission;
import com.vinaacademy.platform.feature.user.auth.service.AuthorizationService;
import com.vinaacademy.platform.feature.user.constant.ResourceConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service để xử lý việc sắp xếp lại thứ tự các bài học
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LessonReorderService {

    private final LessonRepository lessonRepository;
    private final SectionRepository sectionRepository;
    private final AuthorizationService authorizationService;

    /**
     * Sắp xếp lại thứ tự các bài học trong một section
     * @param sectionId ID của section
     * @param lessonIds Danh sách ID các bài học theo thứ tự mới
     */
    @Transactional
    @RequiresResourcePermission(
            resourceType = ResourceConstants.SECTION,
            permission = ResourceConstants.EDIT,
            idParam = "sectionId"
    )
    public void reorderLessons(UUID sectionId, List<UUID> lessonIds) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new NotFoundException("Section not found with id: " + sectionId));

        // Get all lessons for the section
        List<Lesson> lessons = lessonRepository.findBySectionOrderByOrderIndex(section);

        // Validate that all lesson IDs belong to the section
        Set<UUID> sectionLessonIds = lessons.stream()
                .map(Lesson::getId)
                .collect(Collectors.toSet());

        if (!sectionLessonIds.containsAll(lessonIds)) {
            throw BadRequestException.message("Invalid lesson IDs in the list");
        }

        // Validate that all lessons are included
        if (lessons.size() != lessonIds.size()) {
            throw BadRequestException.message("The list does not include all lessons in the section");
        }

        // Create a map for quick lookup
        Map<UUID, Lesson> lessonMap = new HashMap<>();
        lessons.forEach(lesson -> lessonMap.put(lesson.getId(), lesson));

        // Batch update all lessons with their new order indices
        List<Lesson> updatedLessons = new ArrayList<>();
        for (int i = 0; i < lessonIds.size(); i++) {
            UUID lessonId = lessonIds.get(i);
            Lesson lesson = lessonMap.get(lessonId);
            lesson.setOrderIndex(i);
            updatedLessons.add(lesson);
        }

        // Save all lessons in a batch operation
        lessonRepository.saveAll(updatedLessons);

        log.info("Lessons reordered for section: {}", section.getTitle());
    }
}