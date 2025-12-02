package com.vinaacademy.platform.feature.document.service.impl;

import com.vinaacademy.platform.exception.BadRequestException;
import com.vinaacademy.platform.exception.NotFoundException;
import com.vinaacademy.platform.feature.document.service.DocumentService;
import com.vinaacademy.platform.feature.storage.dto.MediaFileDto;
import com.vinaacademy.platform.feature.storage.entity.MediaFile;
import com.vinaacademy.platform.feature.storage.enums.FileType;
import com.vinaacademy.platform.feature.storage.mapper.MediaFileMapper;
import com.vinaacademy.platform.feature.storage.repository.MediaFileRepository;
import com.vinaacademy.platform.feature.storage.service.S3Service;
import com.vinaacademy.platform.feature.user.auth.helpers.SecurityHelper;
import com.vinaacademy.platform.feature.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {
    private final S3Service s3Service;
    private final MediaFileRepository mediaFileRepository;
    private final SecurityHelper securityHelper;
    private final Tika tika = new Tika();

    @Override
    @Transactional
    public MediaFileDto uploadDocument(MultipartFile file) {
        // 1. Validation
        if (file.isEmpty() || file.getSize() == 0 || file.getOriginalFilename() == null) {
            throw BadRequestException.message("File is empty or invalid");
        }

        User user = securityHelper.getCurrentUser();
        
        // 2. Detect MIME type using Apache Tika for better accuracy
        String mimeType;
        try {
            mimeType = tika.detect(file.getInputStream());
        } catch (IOException e) {
            mimeType = file.getContentType();
        }
        
        // 3. Generate S3 key with organized structure
        String dateFolder = LocalDate.now().toString();
        String cleanFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String s3Key = String.format("documents/%s/%s/%s_%s",
                user.getId(),
                dateFolder,
                UUID.randomUUID(),
                cleanFileName);
        
        log.info("Uploading document to S3: key={}, size={}, mimeType={}", 
                s3Key, file.getSize(), mimeType);
        
        // 4. Upload to S3/MinIO
        try {
            String s3Url = s3Service.uploadFile(
                    s3Key,
                    file.getInputStream(),
                    file.getSize(),
                    mimeType
            );
            
            log.debug("Document uploaded to S3: url={}", s3Url);
            
            // 5. Save metadata to database
            MediaFile mediaFile = MediaFile.builder()
                    .fileName(file.getOriginalFilename())
                    .filePath(s3Key)  // Store S3 key, not full URL
                    .fileSize(file.getSize())
                    .fileType(FileType.DOCUMENT)
                    .mimeType(mimeType)
                    .userId(user.getId())
                    .status(MediaFile.UploadStatus.COMPLETED)
                    .build();
            
            mediaFile = mediaFileRepository.save(mediaFile);
            
            log.info("Document metadata saved with ID: {}", mediaFile.getId());
            return MediaFileMapper.INSTANCE.toDto(mediaFile);
            
        } catch (IOException e) {
            log.error("Failed to upload document: {}", e.getMessage(), e);
            throw BadRequestException.message("Failed to upload document: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public MediaFileDto viewDocument(UUID id) {
        MediaFile mediaFile = mediaFileRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Document not found with ID: " + id));
        
        if (mediaFile.getFileType() != FileType.DOCUMENT && mediaFile.getFileType() != FileType.OTHER) {
            throw BadRequestException.message("File is not a document type");
        }
        
        return MediaFileMapper.INSTANCE.toDto(mediaFile);
    }
    
    @Override
    @Transactional(readOnly = true)
    public String generateDownloadUrl(UUID id) {
        MediaFile mediaFile = mediaFileRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Document not found with ID: " + id));
        
        if (mediaFile.getFileType() != FileType.DOCUMENT && mediaFile.getFileType() != FileType.OTHER) {
            throw BadRequestException.message("File is not a document type");
        }
        
        if (mediaFile.getFilePath() == null || mediaFile.getFilePath().isEmpty()) {
            throw BadRequestException.message("Document file path is not available");
        }
        
        // Generate presigned URL valid for 1 hour (3600 seconds)
        String presignedUrl = s3Service.generatePresignedUrl(mediaFile.getFilePath(), 3600);
        
        log.info("Generated presigned download URL for document: id={}", id);
        return presignedUrl;
    }
}
