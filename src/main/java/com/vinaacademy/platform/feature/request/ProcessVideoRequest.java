package com.vinaacademy.platform.feature.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ProcessVideoRequest {
    @NotNull(message = "Video ID không được để trống")
    private UUID videoId;
    @NotNull(message = "Media file ID không được để trống")
    private UUID mediaFileId;
}
