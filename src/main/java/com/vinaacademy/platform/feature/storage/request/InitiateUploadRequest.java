package com.vinaacademy.platform.feature.storage.request;

import com.vinaacademy.platform.feature.storage.enums.FileType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InitiateUploadRequest {
    @NotBlank(message = "Tên tệp không được để trống")
    private String filename;
    @NotNull(message = "Kích thước tệp không được để trống")
    @Min(value = 1, message = "Kích thước tệp phải lớn hơn 0")
    private Long fileSize;
    @NotNull(message = "Loại tệp không được để trống")
    private FileType fileType;
    private String fileHash; // Optional: for resume detection
    @NotNull(message = "Kích thước phân đoạn không được để trống")
    @Min(value = 1, message = "Kích thước phân đoạn phải lớn hơn 0")
    private Integer chunkSize; // Default: 1MB
}