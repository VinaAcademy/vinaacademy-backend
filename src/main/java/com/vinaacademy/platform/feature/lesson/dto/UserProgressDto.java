package com.vinaacademy.platform.feature.lesson.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProgressDto {
    private Long id;
    private UUID userId;
    private String userName;
    private UUID lessonId;
    private String lessonTitle;
    private boolean completed;
    private Long lastWatchedTime;
    private LocalDateTime lastUpdated;
}