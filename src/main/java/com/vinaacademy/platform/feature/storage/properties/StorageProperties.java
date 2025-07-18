package com.vinaacademy.platform.feature.storage.properties;

import com.vinaacademy.platform.feature.storage.enums.FileType;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class StorageProperties {
    @Value("${application.storage.uploadDir:${user.home}/uploads}")
    private String uploadDir;
    @Value("${application.storage.hlsOutputDir:${application.storage.uploadDir}/hls}")
    private String hlsDir;
    @Value("${application.storage.tempDir:${application.storage.uploadDir}/temp}")
    private String tempDir;
    private String videoDir;
    private String imageDir;
    private String thumbnailDir;
    private String documentDir;

    @PostConstruct
    private void init() {
        videoDir = uploadDir + "/videos";
        imageDir = uploadDir + "/images";
        thumbnailDir = uploadDir + "/thumbnails";
        documentDir = uploadDir + "/documents";
    }

    public String getDirByType(FileType fileType) {
        return switch (fileType) {
            case VIDEO -> videoDir;
            case IMAGE -> imageDir;
            case DOCUMENT -> documentDir;
            default -> uploadDir; // Default to upload directory for unknown types
        };
    }
}
