package com.vinaacademy.platform.feature.lesson.service;

import com.vinaacademy.platform.feature.lesson.dto.LessonProgressDto;
import com.vinaacademy.platform.feature.lesson.dto.UserProgressDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface UserProgressService {
    List<UserProgressDto> getProgressByUser(UUID userId, UUID courseId);
    Page<UserProgressDto> getProgressByCourse(UUID courseId, int page, int size);
    Page<UserProgressDto> getProgressByLesson(UUID lessonId, int page, int size);
    UserProgressDto updateProgress(UUID userId, UUID lessonId, boolean completed, Long lastWatchedTime);
}