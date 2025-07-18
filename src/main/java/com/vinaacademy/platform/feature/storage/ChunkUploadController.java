package com.vinaacademy.platform.feature.storage;

import com.vinaacademy.platform.feature.common.response.ApiResponse;
import com.vinaacademy.platform.feature.storage.dto.UploadResult;
import com.vinaacademy.platform.feature.storage.dto.UploadSessionDto;
import com.vinaacademy.platform.feature.storage.request.ChunkUploadRequest;
import com.vinaacademy.platform.feature.storage.request.InitiateUploadRequest;
import com.vinaacademy.platform.feature.storage.service.ChunkUploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/storage/chunk-upload")
@RequiredArgsConstructor
public class ChunkUploadController {
    private final ChunkUploadService chunkUploadService;

    @PostMapping("/initiate")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UploadSessionDto> initiateUpload(@RequestBody @Valid InitiateUploadRequest request) {
        UploadSessionDto session = chunkUploadService.initiateUpload(request);
        return ApiResponse.success(session);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UploadResult> uploadChunk(
            @RequestParam("chunk") MultipartFile chunkFile,
            @ModelAttribute ChunkUploadRequest request) {
        UploadResult session = chunkUploadService.uploadChunk(chunkFile, request);
        return ApiResponse.success(session);
    }

    @GetMapping("/status/{sessionId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UploadSessionDto> getUploadStatus(@PathVariable UUID sessionId) {
        UploadSessionDto session = chunkUploadService.getUploadStatus(sessionId);
        return ApiResponse.success(session);
    }

    @GetMapping("/sessions")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<UploadSessionDto>> getAllUploadSessions() {
        return  ApiResponse.success(chunkUploadService.getAllActiveSessions());
    }

    @DeleteMapping("/cancel/{sessionId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> cancelUpload(@PathVariable UUID sessionId) {
        chunkUploadService.cancelUpload(sessionId);
        return ApiResponse.success(null);
    }
}
