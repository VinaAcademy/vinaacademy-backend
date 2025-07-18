package com.vinaacademy.platform.feature.storage.service.impl;

import com.vinaacademy.platform.exception.BadRequestException;
import com.vinaacademy.platform.feature.storage.dto.UploadResult;
import com.vinaacademy.platform.feature.storage.dto.UploadSessionDto;
import com.vinaacademy.platform.feature.storage.entity.MediaFile;
import com.vinaacademy.platform.feature.storage.mapper.UploadSessionMapper;
import com.vinaacademy.platform.feature.storage.properties.StorageProperties;
import com.vinaacademy.platform.feature.storage.repository.MediaFileRepository;
import com.vinaacademy.platform.feature.storage.request.ChunkUploadRequest;
import com.vinaacademy.platform.feature.storage.request.InitiateUploadRequest;
import com.vinaacademy.platform.feature.storage.service.ChunkUploadService;
import com.vinaacademy.platform.feature.user.auth.helpers.SecurityHelper;
import com.vinaacademy.platform.feature.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChunkUploadServiceImpl implements ChunkUploadService {
    private final MediaFileRepository mediaFileRepository;

    @Value("${application.upload.chunk.default-size:1048576}") // Default 1MB
    private long defaultChunkSize;
    @Value("${application.upload.chunk.max-size:5242880}") // Default 5MB
    private long maxChunkSize;

    @Autowired
    private SecurityHelper securityHelper;
    @Autowired
    private StorageProperties storageProperties;

    @Override
    public UploadSessionDto initiateUpload(InitiateUploadRequest request) {
        User currentUser = securityHelper.getCurrentUser();

        // 1. resume by file hash and user active session
        if (StringUtils.isNotBlank(request.getFileHash())) {
            Optional<MediaFile> existingSessionOpt = mediaFileRepository
                    .findByFileHashAndUserIdAndStatus(request.getFileHash(), currentUser.getId(),
                            MediaFile.UploadStatus.IN_PROGRESS);

            if (existingSessionOpt.isPresent()) {
                log.info("Resuming existing upload session for file hash: {}", request.getFileHash());
                return UploadSessionMapper.INSTANCE.toDto(existingSessionOpt.get());
            }
        }
        // 2. create new session
        MediaFile uploadSession = MediaFile.builder()
                .fileName(request.getFilename())
                .fileSize(request.getFileSize())
                .fileHash(request.getFileHash())
                .fileType(request.getFileType())
                .status(MediaFile.UploadStatus.INITIATED)
                .uploadedChunks(0)
                .chunkSize(request.getChunkSize())
                .totalChunks((int) Math.ceil((double) request.getFileSize() / request.getChunkSize()))
                .userId(currentUser.getId())
                .build();
        // 3. create temporary filepath
        Path tempDir = Paths.get(storageProperties.getTempDir(), currentUser.getId().toString());
        try {
            Files.createDirectories(tempDir);
            Path tempFilePath = tempDir.resolve(String.format("%s-%s", UUID.randomUUID()
                    , request.getFilename()));
            uploadSession.setFilePath(tempFilePath.toString());

            // pre-allocate file space
            try (RandomAccessFile raf = new RandomAccessFile(tempFilePath.toFile(), "rw")) {
                raf.setLength(uploadSession.getFileSize());
            }
        } catch (IOException e) {
            throw BadRequestException.message("Không thể tạo thư mục tạm thời cho tải lên: " + e.getMessage());
        }

        uploadSession = mediaFileRepository.save(uploadSession);
        log.info("Created new upload session: {}", uploadSession.getId());

        return UploadSessionMapper.INSTANCE.toDto(uploadSession);
    }

    @Override
    @Transactional
    public UploadResult uploadChunk(MultipartFile chunkFile, ChunkUploadRequest request) {
        // validate
        MediaFile uploadSession = mediaFileRepository.findById(request.getSessionId())
                .orElseThrow(() -> BadRequestException.message("Không tìm thấy phiên tải lên với ID: " + request.getSessionId()));
        User currentUser = securityHelper.getCurrentUser();
        if (uploadSession.getUserId() != null && !uploadSession.getUserId().equals(currentUser.getId())) {
            throw BadRequestException.message("Bạn không có quyền truy cập vào phiên tải lên này");
        }
        if (uploadSession.getStatus() == MediaFile.UploadStatus.COMPLETED) {
            throw BadRequestException.message("Phiên tải lên đã hoàn thành");
        }
        if (uploadSession.getExpiresAt().isBefore(LocalDateTime.now())) {
            uploadSession.setStatus(MediaFile.UploadStatus.EXPIRED);
            mediaFileRepository.save(uploadSession);
            throw BadRequestException.message("Phiên tải lên đã hết hạn");
        }

        validateChunk(chunkFile, request.getChunkNumber(), uploadSession);

        // write chunk to temp file
        try {
            Path tempFilePath = Paths.get(uploadSession.getFilePath());
            try (RandomAccessFile raf = new RandomAccessFile(tempFilePath.toFile(), "rw")) {
                raf.seek((long) request.getChunkNumber() * uploadSession.getChunkSize());
                raf.write(chunkFile.getBytes());
            }
        } catch (IOException e) {
            log.error("Failed to write chunk to temporary file: {}", e.getMessage());
            throw BadRequestException.message("Không thể ghi chunk vào tệp tạm thời: " + e.getMessage());
        }

        // update session
        boolean isLastChunk = request.getChunkNumber() == uploadSession.getTotalChunks() - 1;
        uploadSession.setUploadedChunks(uploadSession.getUploadedChunks() + 1);
        if (isLastChunk) {
            uploadSession.setStatus(MediaFile.UploadStatus.COMPLETED);
            // check hash match if provided
            if (StringUtils.isNotBlank(uploadSession.getFileHash())) {
                boolean hashMatch = checkHashMatch(Paths.get(uploadSession.getFilePath()), uploadSession.getFileHash());
                if (!hashMatch) {
                    throw BadRequestException.message("Hash của tệp tải lên không khớp");
                }
            }
        } else {
            uploadSession.setStatus(MediaFile.UploadStatus.IN_PROGRESS);
        }
        uploadSession = mediaFileRepository.save(uploadSession);

        log.debug("Uploaded chunk {} for session {}. Total uploaded: {}/{}",
                request.getChunkNumber(), uploadSession.getId(), uploadSession.getUploadedChunks(),
                uploadSession.getTotalChunks());

        return UploadSessionMapper.INSTANCE.toDto(uploadSession);
    }

    private boolean checkHashMatch(Path tempFilePath, String expectedHash) {
        try (InputStream is = Files.newInputStream(tempFilePath)) {
            String actualHash = DigestUtils.sha256Hex(is);
            return actualHash.equalsIgnoreCase(expectedHash);
        } catch (IOException e) {
            log.error("Failed to read temporary file for hash check: {}", e.getMessage());
            return false;
        }
    }

    private void validateChunk(MultipartFile chunkFile, Integer chunkNumber, MediaFile uploadSession) {
        if (chunkNumber < 0 || chunkNumber >= uploadSession.getTotalChunks()) {
            throw BadRequestException.message("Số lượng chunk không hợp lệ: " + chunkNumber);
        }
        if (chunkFile.isEmpty()) {
            throw BadRequestException.message("Chunk file không được để trống");
        }
        int expectedSize = uploadSession.getChunkSize();
        if (chunkNumber == uploadSession.getTotalChunks() - 1) {
            // last chunk may be smaller
            expectedSize = (int) (uploadSession.getFileSize() % uploadSession.getChunkSize());
            if (expectedSize == 0) {
                expectedSize = uploadSession.getChunkSize();
            }
        }

        if (expectedSize != chunkFile.getSize()) {
            throw BadRequestException.message(String.format("Kích thước chunk không hợp lệ: %d bytes, expected: %d bytes",
                    chunkFile.getSize(), expectedSize));
        }
    }

    @Override
    public UploadSessionDto getUploadStatus(UUID sessionId) {
        MediaFile uploadSession = mediaFileRepository.findById(sessionId)
                .orElseThrow(() ->
                        BadRequestException.message("Không tìm thấy phiên tải lên với ID: " + sessionId));

        User currentUser = securityHelper.getCurrentUser();
        if (uploadSession.getUserId() != null && !uploadSession.getUserId().equals(currentUser.getId())) {
            throw BadRequestException.message("Bạn không có quyền truy cập vào phiên tải lên này");
        }
        return UploadSessionMapper.INSTANCE.toDto(uploadSession);
    }

    @Override
    public List<UploadSessionDto> getAllActiveSessions() {
        User currentUser = securityHelper.getCurrentUser();
        List<MediaFile> activeSessions = mediaFileRepository
                .findByUserIdAndStatus(currentUser.getId(), MediaFile.UploadStatus.IN_PROGRESS);
        return UploadSessionMapper.INSTANCE.toDtoList(activeSessions);
    }

    @Override
    @Transactional
    public void cancelUpload(UUID sessionId) {
        // 1. validate
        MediaFile uploadSession = mediaFileRepository.findById(sessionId)
                .orElseThrow(() -> BadRequestException.message("Không tìm thấy phiên tải lên với ID: " + sessionId));

        User currentUser = securityHelper.getCurrentUser();
        if (uploadSession.getUserId() != null && !uploadSession.getUserId().equals(currentUser.getId())) {
            throw BadRequestException.message("Bạn không có quyền hủy phiên tải lên này");
        }

        // 2. remove temporary files
        try {
            Path path = Paths.get(uploadSession.getFilePath());
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("Failed to delete temporary file for upload session {}: {}", sessionId, e.getMessage());
        }

        // 3. delete upload session
        mediaFileRepository.delete(uploadSession);
        log.info("Upload session {} has been cancelled and deleted", sessionId);
    }
}
