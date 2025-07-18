package com.vinaacademy.platform.feature.image.service;

import com.vinaacademy.platform.feature.storage.dto.MediaFileDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface ImageService {
    MediaFileDto uploadImage(MultipartFile file);

    MediaFileDto viewImage(UUID id);
}
