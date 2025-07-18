package com.vinaacademy.platform.feature.storage.dto;

import com.vinaacademy.platform.feature.storage.entity.MediaFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadSessionDto implements UploadResult {
    private UUID sessionId;
    private String filename;
    private Long fileSize;
    private Integer chunkSize;
    private Integer totalChunks;
    private Integer uploadedChunks;
    private MediaFile.UploadStatus status;
    private LocalDateTime expiresAt;
    private Double progressPercentage;
}