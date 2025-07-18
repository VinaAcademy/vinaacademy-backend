package com.vinaacademy.platform.feature.storage.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vinaacademy.platform.feature.storage.enums.FileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.io.Resource;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class MediaFileDto implements UploadResult {
    private UUID id;
    private UUID userId;
    private String fileName;
    private FileType fileType;
    private String mimeType;
    private long fileSize;
    @JsonIgnore
    private String filePath;
    private Resource fileResource;
}
