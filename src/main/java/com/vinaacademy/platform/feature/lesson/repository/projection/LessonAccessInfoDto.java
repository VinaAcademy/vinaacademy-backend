package com.vinaacademy.platform.feature.lesson.repository.projection;

import java.util.UUID;

public record LessonAccessInfoDto(
        UUID lessonId,
        String lessonTitle,
        boolean isFree,
        boolean isInstructor,
        boolean isEnrolled
) {
}