package com.vinaacademy.platform.feature.lesson.dto;

import java.util.List;
import java.util.UUID;

import com.vinaacademy.platform.feature.course.enums.LessonStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonReviewRequest {
    @NotNull(message = "Lesson IDs cannot be null")
    private List<UUID> lessonIds;
    @NotNull(message = "Status cannot be null")
    private LessonStatus status;
    @Size(max = 2000, min = 1)
    private String content;
}
