package com.vinaacademy.platform.feature.storage.utils;

import com.vinaacademy.platform.feature.storage.enums.FileType;
import com.vinaacademy.platform.feature.storage.properties.StorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.nio.file.Path;

@Component
@RequiredArgsConstructor
public class StorageUtils {
    private final StorageProperties storageProperties;

    public String getUploadDirByType(FileType type) {
        switch (type) {
            case VIDEO:
                return storageProperties.getVideoDir();
            case IMAGE:
                return storageProperties.getImageDir();
            case DOCUMENT:
                return storageProperties.getUploadDir();
            default:
                return storageProperties.getUploadDir();
        }
    }

    public Resource loadAsResource(Path filePath) throws MalformedURLException {
        return new UrlResource(filePath.toUri());
    }
}
