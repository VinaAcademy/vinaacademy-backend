package com.vinaacademy.platform.feature.lesson.service;

import com.vinaacademy.platform.feature.lesson.dto.LessonDto;
import com.vinaacademy.platform.feature.lesson.dto.LessonRequest;
import com.vinaacademy.platform.feature.lesson.entity.Lesson;
import com.vinaacademy.platform.feature.user.entity.User;

import java.util.List;
import java.util.UUID;

public interface LessonService {
    LessonDto getLessonById(UUID id);

    List<LessonDto> getLessonsBySectionId(UUID sectionId);

    LessonDto createLesson(LessonRequest request);

    LessonDto createLesson(LessonRequest request, User author); // New method with explicit author

    LessonDto updateLesson(UUID id, LessonRequest request);

    void deleteLesson(UUID id);

    void completeLesson(UUID lessonId);

    void markLessonCompleted(Lesson lesson, User user);
}
