package com.vinaacademy.platform.feature.video.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoNoteRequestDto {
    @NotNull(message = "Video ID không được để trống")
    private UUID videoId;

    @NotNull(message = "Thời điểm ghi chú không được để trống")
    private Long timeStampSeconds;

    @NotBlank(message = "Nội dung ghi chú không được để trống")
    private String noteText;
}
