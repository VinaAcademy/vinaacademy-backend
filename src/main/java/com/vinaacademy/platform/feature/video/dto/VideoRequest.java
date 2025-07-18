package com.vinaacademy.platform.feature.video.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoRequest {
    @NotNull(message = "Lesson Id không được để trống")
    private UUID lessonId;
    private String thumbnailUrl;
}
