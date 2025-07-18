package com.vinaacademy.platform.feature.storage.repository;

import com.vinaacademy.platform.feature.storage.entity.MediaFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MediaFileRepository extends JpaRepository<MediaFile, UUID> {

    List<MediaFile> findByUserIdAndStatus(UUID userId, MediaFile.UploadStatus status);

    Optional<MediaFile> findByFileHashAndUserIdAndStatus(String fileHash, UUID userId, MediaFile.UploadStatus status);
}
