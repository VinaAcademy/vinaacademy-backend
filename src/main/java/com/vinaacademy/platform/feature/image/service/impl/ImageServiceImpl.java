package com.vinaacademy.platform.feature.image.service.impl;

import com.vinaacademy.platform.feature.image.service.ImageService;
import com.vinaacademy.platform.feature.storage.dto.MediaFileDto;
import com.vinaacademy.platform.feature.storage.enums.FileType;
import com.vinaacademy.platform.feature.storage.service.StorageService;
import com.vinaacademy.platform.feature.user.auth.helpers.SecurityHelper;
import com.vinaacademy.platform.feature.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {
    private final StorageService storageService;

    private final SecurityHelper securityHelper;

    @Override
    public MediaFileDto uploadImage(MultipartFile file) {
        User user = securityHelper.getCurrentUser();
        MediaFileDto mediaFileDto;
        try {
            mediaFileDto = storageService
                    .uploadFile(file, FileType.IMAGE, user.getId().toString());
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image", e);
        }
        return mediaFileDto;
    }

    @Override
    public MediaFileDto viewImage(UUID id) {
        MediaFileDto mediaFileDto = storageService.loadFile(id);
        if (mediaFileDto == null) {
            throw new RuntimeException("Image not found");
        }
        return mediaFileDto;
    }
}
