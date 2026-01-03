package com.vinaacademy.platform.feature.lesson.dto;

import com.vinaacademy.platform.feature.course.enums.LessonStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonReviewRequest {
    @NotNull(message = "Lesson IDs cannot be null")
    private List<UUID> lessonIds;
    @NotNull(message = "Status cannot be null")
    private LessonStatus status;
    private String content;
}
