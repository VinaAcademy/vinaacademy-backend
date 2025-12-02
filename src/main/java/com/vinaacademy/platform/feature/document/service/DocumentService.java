package com.vinaacademy.platform.feature.document.service;

import com.vinaacademy.platform.feature.storage.dto.MediaFileDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface DocumentService {
    MediaFileDto uploadDocument(MultipartFile file);

    MediaFileDto viewDocument(UUID id);
    
    String generateDownloadUrl(UUID id);
}
