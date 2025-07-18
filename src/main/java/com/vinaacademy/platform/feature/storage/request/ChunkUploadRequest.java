package com.vinaacademy.platform.feature.storage.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChunkUploadRequest {
    private UUID sessionId;
    private Integer chunkNumber;
    private String chunkHash; // Optional: for chunk integrity verification
}