package com.vinaacademy.platform.feature.lesson.service;

import com.vinaacademy.platform.feature.lesson.dto.LessonProgressDto;

import java.util.List;
import java.util.UUID;

public interface LessonProgressService {
    List<LessonProgressDto> getAllLessonProgressByCourse(UUID courseId);
}