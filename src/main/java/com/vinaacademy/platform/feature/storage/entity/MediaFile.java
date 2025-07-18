package com.vinaacademy.platform.feature.storage.entity;

import com.vinaacademy.platform.feature.lesson.entity.Lesson;
import com.vinaacademy.platform.feature.storage.enums.FileType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "media_files")
public class MediaFile {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private FileType fileType;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "size")
    private long fileSize;

    @Column(name = "file_path")
    private String filePath;

    @ManyToMany(mappedBy = "mediaFiles")
    private List<Lesson> lessons;

    // Các trường từ UploadSession
    @Column(name = "chunk_size")
    private Integer chunkSize;

    @Column(name = "total_chunks")
    private Integer totalChunks;

    @Column(name = "uploaded_chunks")
    private Integer uploadedChunks;

    @Column(name = "file_hash")
    private String fileHash;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private UploadStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.expiresAt = this.createdAt.plusDays(1);
        if (this.status == null) {
            this.status = UploadStatus.INITIATED;
        }
    }

    public enum UploadStatus {
        INITIATED,
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        EXPIRED
    }
}

