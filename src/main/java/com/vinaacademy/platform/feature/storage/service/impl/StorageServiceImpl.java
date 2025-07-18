package com.vinaacademy.platform.feature.storage.service.impl;

import com.vinaacademy.platform.feature.storage.dto.MediaFileDto;
import com.vinaacademy.platform.feature.storage.entity.MediaFile;
import com.vinaacademy.platform.feature.storage.enums.FileType;
import com.vinaacademy.platform.feature.storage.mapper.MediaFileMapper;
import com.vinaacademy.platform.feature.storage.repository.MediaFileRepository;
import com.vinaacademy.platform.feature.storage.service.StorageService;
import com.vinaacademy.platform.feature.storage.utils.StorageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageService {
    private final StorageUtils storageUtils;

    private final MediaFileRepository mediaFileRepository;

    @Override
    public MediaFileDto uploadFile(MultipartFile file, FileType fileType, String userId) throws IOException {
        if (file.isEmpty() || file.getSize() == 0 || file.getOriginalFilename() == null) {
            throw new IllegalArgumentException("File is empty");
        }
        String dateFolder = LocalDate.now().toString();
        String uploadDir = storageUtils.getUploadDirByType(fileType);
        String fileName = String.format("%s_%s", UUID.randomUUID(),
                StringUtils.cleanPath(file.getOriginalFilename()));

        Path userPath = Paths.get(uploadDir, userId, dateFolder);
        Files.createDirectories(userPath);

        Path filePath = userPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        MediaFile mediaFile = MediaFile.builder()
                .fileName(file.getOriginalFilename())
                .filePath(filePath.toString())
                .fileSize(file.getSize())
                .fileType(fileType)
                .mimeType(file.getContentType())
                .userId(UUID.fromString(userId))
                .build();
        mediaFile = mediaFileRepository.save(mediaFile);
        return MediaFileMapper.INSTANCE.toDto(mediaFile);
    }

    @Override
    public MediaFileDto loadFile(UUID id) {
        MediaFile mediaFile = mediaFileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));
        Path filePath = Paths.get(mediaFile.getFilePath());
        Resource resource;
        try {
            resource = storageUtils.loadAsResource(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Could not read file: " + filePath, e);
        }
        MediaFileDto mediaFileDto = MediaFileMapper.INSTANCE.toDto(mediaFile);
        mediaFileDto.setFileResource(resource);
        return mediaFileDto;
    }

    @Override
    public MediaFileDto getMediaFileById(UUID id) {
        MediaFile mediaFile = mediaFileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));
        return MediaFileMapper.INSTANCE.toDto(mediaFile);
    }
}
