package com.vinaacademy.platform.feature.storage.service;

import com.vinaacademy.platform.feature.storage.dto.UploadResult;
import com.vinaacademy.platform.feature.storage.dto.UploadSessionDto;
import com.vinaacademy.platform.feature.storage.request.ChunkUploadRequest;
import com.vinaacademy.platform.feature.storage.request.InitiateUploadRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ChunkUploadService {
    UploadSessionDto initiateUpload(InitiateUploadRequest request);

    UploadResult uploadChunk(MultipartFile chunkFile, ChunkUploadRequest request);

    UploadSessionDto getUploadStatus(UUID sessionId);

    List<UploadSessionDto> getAllActiveSessions();

    void cancelUpload(UUID sessionId);
}
