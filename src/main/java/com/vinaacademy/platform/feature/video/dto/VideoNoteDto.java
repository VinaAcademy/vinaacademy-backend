package com.vinaacademy.platform.feature.video.dto;

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
public class VideoNoteDto {
    private Long id;
    private UUID userId;
    private Long videoId;
    private Long timeStampSeconds;
    private String noteText;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
