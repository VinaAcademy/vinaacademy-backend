package com.vinaacademy.platform.feature.storage.service;

import com.vinaacademy.platform.feature.storage.dto.MediaFileDto;
import com.vinaacademy.platform.feature.storage.enums.FileType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

public interface StorageService {
    MediaFileDto uploadFile(MultipartFile file, FileType fileType, String userId) throws IOException;

    MediaFileDto loadFile(UUID id);

    MediaFileDto getMediaFileById(UUID id);
}
