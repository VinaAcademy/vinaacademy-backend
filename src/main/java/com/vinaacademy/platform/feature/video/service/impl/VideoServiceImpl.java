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

  /**
   * Uploads a video file for a lesson, marks the video as processing and triggers asynchronous processing.
   *
   * Validates the uploaded file, checks that the current user has access to the lesson (instructor or admin),
   * uploads the file to storage, updates and saves the Video entity (sets thumbnail, status=PROCESSING, duration=0, author),
   * and starts asynchronous processing via the video processor service.
   *
   * @param file the uploaded video file
   * @param videoRequest request data containing the target lesson ID and thumbnail URL
   * @return the saved Video as a VideoDto (with updated status and metadata)
   * @throws IOException if an I/O error occurs while uploading the file
   * @throws BadRequestException when the lesson or video is not found, the user lacks access, or the video is already processing
   */
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

  /**
   * Return a redirect response to the video's thumbnail URL.
   *
   * If the stored thumbnail is an S3 key (not starting with "http://"/"https://"), verifies the object exists
   * in S3 and returns a 302 redirect to a presigned URL (valid 86400s) with Cache-Control max-age=86400.
   * If the stored thumbnail is an absolute URL, returns a 302 redirect directly to that URL with the same cache header.
   *
   * @param videoId the UUID of the video whose thumbnail is requested
   * @return a 302 ResponseEntity that redirects the client to the thumbnail (presigned S3 URL or external URL)
   * @throws BadRequestException if the video is not found, the thumbnail URL is missing, the thumbnail does not exist in storage,
   *         or an error occurs while generating/serving the thumbnail
   */
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
   * Rewrite an HLS playlist's relative segment and playlist references to backend streaming endpoints.
   *
   * <p>Lines that are empty or begin with '#' (comments/metadata) are preserved unchanged. Lines that
   * end with ".ts" or ".m3u8" are replaced with a backend URL of the form
   * "/api/v1/videos/{videoId}/{originalLine}". All other lines are left as-is. The method returns
   * the transformed playlist content with newline separators preserved.
   *
   * @param videoId the UUID of the video used to build the backend streaming path
   * @param playlistContent the original playlist text (UTF-8 HLS manifest) to be rewritten
   * @return the playlist content with segment and playlist references rewritten to backend endpoints
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

  /**
   * Returns a presigned S3 URL for streaming a specific HLS segment of a video.
   *
   * Validates the requested subPath, ensures the video exists and is READY, verifies the segment exists in S3,
   * and generates a presigned URL valid for 1 hour.
   *
   * @param videoId the UUID of the video
   * @param subPath the relative path to the HLS segment or manifest (must match [A-Za-z0-9_./-]+)
   * @return a presigned URL that provides direct access to the requested segment for 1 hour
   * @throws BadRequestException if the subPath is invalid, the video is not found, the video is not READY, or the segment does not exist
   */
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

  /**
   * Downloads an HLS manifest for the given video, rewrites non-comment lines to backend-proxied paths, and returns the rewritten manifest.
   *
   * <p>All lines starting with `#` or empty lines are preserved. Non-comment lines are treated as segment or playlist references:
   * - If a line is an absolute URL, only the final path component (filename) is extracted and used.
   * - The resulting relative path (which may include subdirectories) is prefixed with {@code basePath} so the client will request segments via the backend.
   *
   * @param videoId the video UUID used to locate the HLS manifest in storage
   * @param basePath the backend prefix to prepend to each non-comment manifest line (should include a trailing slash if needed)
   * @param subPath the path under the video's HLS location identifying the manifest to rewrite
   * @return a ByteArrayResource containing the rewritten manifest encoded as UTF-8
   * @throws IOException if reading the original manifest from storage fails
   */
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
