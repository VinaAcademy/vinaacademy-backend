package com.vinaacademy.platform.feature.video.service.impl;

import com.vinaacademy.platform.exception.BadRequestException;
import com.vinaacademy.platform.feature.lesson.repository.LessonRepository;
import com.vinaacademy.platform.feature.lesson.repository.projection.LessonAccessInfoDto;
import com.vinaacademy.platform.feature.storage.dto.MediaFileDto;
import com.vinaacademy.platform.feature.storage.enums.FileType;
import com.vinaacademy.platform.feature.storage.service.S3Service;
import com.vinaacademy.platform.feature.storage.service.StorageService;
import com.vinaacademy.platform.feature.user.auth.helpers.SecurityHelper;
import com.vinaacademy.platform.feature.user.constant.AuthConstants;
import com.vinaacademy.platform.feature.user.entity.User;
import com.vinaacademy.platform.feature.video.dto.VideoDto;
import com.vinaacademy.platform.feature.video.dto.VideoRequest;
import com.vinaacademy.platform.feature.video.entity.Video;
import com.vinaacademy.platform.feature.video.enums.VideoStatus;
import com.vinaacademy.platform.feature.video.mapper.VideoMapper;
import com.vinaacademy.platform.feature.video.repository.VideoRepository;
import com.vinaacademy.platform.feature.video.service.VideoService;
import com.vinaacademy.platform.feature.video.validator.VideoValidator;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class VideoServiceImpl implements VideoService {
  @Autowired private VideoRepository videoRepository;
  @Autowired private LessonRepository lessonRepository;
  @Autowired private VideoProcessorServiceImpl videoProcessorService;
  @Autowired private StorageService storageService;
  @Autowired private S3Service s3Service;

  @Autowired private VideoValidator videoValidator;
  @Autowired private SecurityHelper securityHelper;

  @Override
  public VideoDto uploadVideo(MultipartFile file, VideoRequest videoRequest) throws IOException {
    Video video =
        videoRepository
            .findByIdWithLock(videoRequest.getLessonId())
            .orElseThrow(() -> BadRequestException.message("Không tìm thấy bài học"));
    videoValidator.validate(file);

    User currentUser = securityHelper.getCurrentUser();

    LessonAccessInfoDto lessonAccessInfo =
        lessonRepository
            .getLessonAccessInfo(videoRequest.getLessonId(), currentUser.getId())
            .orElseThrow(() -> BadRequestException.message("Không tìm thấy bài học"));
    if (!lessonAccessInfo.isInstructor() && !securityHelper.hasRole(AuthConstants.ADMIN_ROLE)) {
      throw BadRequestException.message("Bạn không có quyền truy cập vào video này");
    }
    if (VideoStatus.PROCESSING.equals(video.getStatus())) {
      throw BadRequestException.message("Video đang được xử lý");
    }
    video.setThumbnailUrl(videoRequest.getThumbnailUrl());
    video.setStatus(VideoStatus.PROCESSING);
    video.setDuration(0);
    video.setAuthor(currentUser);

    // Tạo thư mục lưu video
    MediaFileDto mediaFile =
        storageService.uploadFile(file, FileType.VIDEO, currentUser.getId().toString());
    String destinationFile = mediaFile.getFilePath();
    video = videoRepository.save(video);

    // Xử lý FFmpeg async
    videoProcessorService.processVideo(video.getId(), Paths.get(destinationFile));

    return VideoMapper.INSTANCE.toDto(video);
  }

  @Override
  public ResponseEntity<Resource> getThumbnail(UUID videoId) {
    log.debug("Getting thumbnail for video: {}", videoId);

    Video video =
        videoRepository
            .findById(videoId)
            .orElseThrow(() -> BadRequestException.message("Không tìm thấy video"));

    if (video.getThumbnailUrl() == null || video.getThumbnailUrl().isEmpty()) {
      log.warn("Thumbnail URL not set for video: {}", videoId);
      throw BadRequestException.message("Thumbnail URL not set");
    }

    String thumbnailKey = video.getThumbnailUrl();

    try {
      // Check if it's a MinIO key (doesn't start with http)
      if (!thumbnailKey.startsWith("http://") && !thumbnailKey.startsWith("https://")) {
        // It's a MinIO S3 key, redirect to presigned URL for direct access
        if (!s3Service.fileExists(thumbnailKey)) {
          log.warn("Thumbnail not found in MinIO: {}", thumbnailKey);
          throw BadRequestException.message("Thumbnail not found");
        }

        // Generate presigned URL for direct access (1 day expiration)
        String presignedUrl = s3Service.generatePresignedUrl(thumbnailKey, 86400);

        log.debug("Redirecting to presigned URL for thumbnail: {}", presignedUrl);
        return ResponseEntity.status(302)
            .header(HttpHeaders.LOCATION, presignedUrl)
            .header(HttpHeaders.CACHE_CONTROL, "max-age=86400") // Cache for 1 day
            .build();
      }
      // For backward compatibility with old URLs - redirect directly
      else {
        return ResponseEntity.status(302)
            .header(HttpHeaders.LOCATION, thumbnailKey)
            .header(HttpHeaders.CACHE_CONTROL, "max-age=86400")
            .build();
      }

    } catch (Exception e) {
      log.error(
          "Error serving thumbnail: videoId={}, thumbnailKey={}, error={}",
          videoId,
          thumbnailKey,
          e.getMessage(),
          e);
      throw BadRequestException.message("Error serving thumbnail");
    }
  }

  /**
   * Process playlist content to replace relative URLs with our streaming endpoints
   *
   * @param videoId the video UUID
   * @param playlistContent the original playlist content
   * @return modified playlist content with our streaming URLs
   */
  private String processPlaylistUrls(UUID videoId, String playlistContent) {
    String[] lines = playlistContent.split("\n");
    StringBuilder modifiedPlaylist = new StringBuilder();

    for (String line : lines) {
      if (line.trim().isEmpty() || line.startsWith("#")) {
        // Keep comments and empty lines as-is
        modifiedPlaylist.append(line).append("\n");
      } else if (line.endsWith(".ts") || line.endsWith(".m3u8")) {
        // Replace relative URLs with our streaming endpoints
        String streamingUrl = "/api/v1/videos/" + videoId + "/" + line.trim();
        modifiedPlaylist.append(streamingUrl).append("\n");
      } else {
        // Keep other lines as-is
        modifiedPlaylist.append(line).append("\n");
      }
    }

    return modifiedPlaylist.toString();
  }

  @Override
  public String getSegmentStreaming(UUID videoId, String subPath) {
    if (!subPath.matches("[A-Za-z0-9_./-]+")) {
      throw BadRequestException.message("Đường dẫn không hợp lệ");
    }

    log.debug("Getting direct streaming URL from MinIO: {}/{}", videoId, subPath);

    Video video =
        videoRepository
            .findById(videoId)
            .orElseThrow(() -> BadRequestException.message("Không tìm thấy video"));

    if (video.getStatus() != VideoStatus.READY) {
      log.warn("Video {} is not ready for streaming, status: {}", videoId, video.getStatus());
      throw BadRequestException.message("Video chưa sẵn sàng để phát");
    }
    // Construct S3 key from video's HLS path and subPath
    String s3Key = video.getHlsPath() + "/" + subPath;

    // Check if file exists in MinIO
    if (!s3Service.fileExists(s3Key)) {
      log.warn("Video segment not found in MinIO: {}", s3Key);
      throw BadRequestException.message("Không tìm thấy tệp video");
    }

    // Generate presigned URL for direct access (1 hour expiration)
    String presignedUrl = s3Service.generatePresignedUrl(s3Key, 3600);

    log.debug("Generated presigned URL for direct streaming: {}", presignedUrl);
    return presignedUrl;
  }

  @Override
  public ByteArrayResource getRewriteManifestProxy(UUID videoId, String basePath, String subPath)
      throws IOException {
    // 2.1 Đọc manifest gốc từ storage
    Video video =
        videoRepository
            .findById(videoId)
            .orElseThrow(() -> BadRequestException.message("Không tìm thấy video"));
    String key = video.getHlsPath() + "/" + subPath;
    String original;
    try (InputStream inputStream = s3Service.downloadFile(key)) {
      original = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }

    // 2.2 Rewrite MỌI dòng không phải comment (#...) thành đường dẫn backend
    // - Giữ nguyên comment (#EXTM3U, #EXT-X-...)
    // - Với dòng là URL tuyệt đối (http/https), ta chỉ lấy phần tên file cuối (an toàn nhất)
    //   rồi gắn vào basePath để luôn đi qua backend (áp quyền + ký segment động)
    String rewritten =
        original
            .lines()
            .map(
                line -> {
                  String trimmed = line.trim();
                  if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    return line; // giữ nguyên comment/metadata
                  }

                  String target = trimmed;

                  // Nếu là URL tuyệt đối (http/https), chỉ lấy phần path sau videoId
                  if (target.contains("://")) {
                    try {
                      URI uri = URI.create(target);
                      target = Paths.get(uri.getPath()).getFileName().toString();
                    } catch (Exception e) {
                      log.warn("Invalid URL in manifest: {}", target, e);
                    }
                  }

                  // Gắn nguyên relative path (có thể bao gồm 720p/segment001.ts)
                  return basePath + target;
                })
            .collect(Collectors.joining("\n"));

    // 2.3 Trả manifest đã rewrite
    byte[] bytes = rewritten.getBytes(StandardCharsets.UTF_8);
    return new ByteArrayResource(bytes);
  }
}
