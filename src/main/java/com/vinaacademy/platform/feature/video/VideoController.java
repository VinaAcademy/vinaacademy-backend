package com.vinaacademy.platform.feature.video;

import com.vinaacademy.platform.feature.common.response.ApiResponse;
import com.vinaacademy.platform.feature.request.ProcessVideoRequest;
import com.vinaacademy.platform.feature.user.auth.annotation.HasAnyRole;
import com.vinaacademy.platform.feature.user.auth.annotation.RequiresResourcePermission;
import com.vinaacademy.platform.feature.user.constant.AuthConstants;
import com.vinaacademy.platform.feature.user.constant.ResourceConstants;
import com.vinaacademy.platform.feature.video.dto.VideoDto;
import com.vinaacademy.platform.feature.video.dto.VideoRequest;
import com.vinaacademy.platform.feature.video.service.VideoProcessorService;
import com.vinaacademy.platform.feature.video.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;
import software.amazon.awssdk.services.s3.S3Client;

@RestController
@RequestMapping("/api/v1/videos")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Videos", description = "Video management APIs")
public class VideoController {
  private final VideoService videoService;
  private final VideoProcessorService videoProcessorService;
  private final S3Client s3Client;

  /**
   * Uploads a video file and associated metadata for a lesson, returning the created VideoDto.
   *
   * @param file the video file to upload (multipart/form-data)
   * @param videoRequest metadata for the video; must include the target lesson ID and other validated fields
   * @return ApiResponse containing the uploaded VideoDto on success
   * @throws IOException if an I/O error occurs while processing the uploaded file
   */
  @Operation(summary = "Upload a video", description = "Upload a video file for a lesson")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Video uploaded successfully",
            content = @Content(schema = @Schema(implementation = VideoDto.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid request"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Unauthorized access"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Không tìm thấy bài học")
      })
  @HasAnyRole({AuthConstants.ADMIN_ROLE, AuthConstants.INSTRUCTOR_ROLE})
  @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiResponse<VideoDto> uploadVideo(
      @Parameter(description = "Video file") @RequestParam("file") MultipartFile file,
      @Parameter(
              description = "Metadata JSON",
              content =
                  @Content(
                      mediaType = "application/json",
                      schema = @Schema(implementation = VideoRequest.class)))
          @RequestPart("metadata")
          @Valid
          VideoRequest videoRequest)
      throws IOException {
    log.debug("Uploading video for lesson: {}", videoRequest.getLessonId());
    return ApiResponse.success(
        "Video uploaded successfully", videoService.uploadVideo(file, videoRequest));
  }

  /**
   * Initiates processing of a lesson video according to the given request.
   *
   * The request is validated and delegated to the video processing service; this method
   * does not wait for processing to complete — it returns a success ApiResponse indicating
   * that processing has been started.
   *
   * @param processVideoRequest contains the target video ID and processing options
   * @return an ApiResponse with a success message (no payload)
   */
  @Operation(summary = "Process video", description = "Process a video file for a lesson")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Video processed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid request"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Unauthorized access"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Video not found")
      })
  @PostMapping("/process")
  @HasAnyRole({AuthConstants.ADMIN_ROLE, AuthConstants.INSTRUCTOR_ROLE})
  @RequiresResourcePermission(
      resourceType = ResourceConstants.LESSON,
      idParam = "processVideoRequest.videoId",
      permission = ResourceConstants.EDIT)
  public ApiResponse<Void> processVideo(
      @RequestBody @Valid ProcessVideoRequest processVideoRequest) {
    log.debug("Processing video: {}", processVideoRequest.getVideoId());
    videoProcessorService.processVideo(processVideoRequest);
    return ApiResponse.success("Video processing started successfully");
  }

  /**
   * Serve a video resource (HLS manifest or media segment) for streaming.
   *
   * <p>Determines the path segment after {videoId} from the incoming request. If the subpath ends
   * with ".m3u8" the method returns a proxied and rewritten HLS manifest (200 OK,
   * Content-Type "application/vnd.apple.mpegurl") with a short private cache. Otherwise it
   * redirects (302 Found) to a short-lived presigned URL for the requested segment or static file.
   *
   * @param request the HTTP servlet request (used to extract the subpath after the {videoId})
   * @param videoId the UUID of the video resource
   * @return a ResponseEntity containing either the manifest resource (200) or a redirect (302)
   * @throws IOException if an I/O error occurs while obtaining or rewriting the manifest or segment URL
   */
  @Operation(summary = "Get video segment", description = "Get a video segment for streaming")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Video segment retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized access"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Video segment not found")
      })
  @PreAuthorize("isAuthenticated()")
  @GetMapping("/{videoId}/**")
  @RequiresResourcePermission(resourceType = ResourceConstants.LESSON, idParam = "videoId")
  public ResponseEntity<Resource> getSegment(
      HttpServletRequest request, @PathVariable UUID videoId) throws IOException {

    // 1) Lấy subPath sau {videoId} (vd: "720p/segment001.ts" hoặc "master.m3u8")
    String fullPath =
        (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
    String bestMatchPattern =
        (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
    String subPath = new AntPathMatcher().extractPathWithinPattern(bestMatchPattern, fullPath);

    log.debug("Streaming entry: videoId={}, subPath={}", videoId, subPath);

    // 2) Nếu là manifest (.m3u8) -> proxy + rewrite
    if (subPath.endsWith(".m3u8")) {
      String requestUri = request.getRequestURI();
      int cut = requestUri.lastIndexOf("/") + 1;
      String basePath = cut > 0 ? requestUri.substring(0, cut) : requestUri;
      if (!basePath.endsWith("/")) basePath += "/";

      ByteArrayResource body = videoService.getRewriteManifestProxy(videoId, basePath, subPath);

      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_TYPE, "application/vnd.apple.mpegurl")
          // Cache ngắn để hạn chế bị share link manifest
          .header(HttpHeaders.CACHE_CONTROL, "private, max-age=10")
          .body(body);
    }

    // 3) Ngược lại: segment hoặc file tĩnh -> 302 redirect sang presigned URL (expire ngắn)
    String presignedUrl = videoService.getSegmentStreaming(videoId, subPath);

    return ResponseEntity.status(HttpStatus.FOUND)
        .header(HttpHeaders.LOCATION, presignedUrl)
        // Cache cực ngắn ở client để giảm request lặp (tùy chỉnh)
        .header(HttpHeaders.CACHE_CONTROL, "private, max-age=5")
        .build();
  }

  /**
   * Retrieve the thumbnail image for a video.
   *
   * Returns a ResponseEntity containing the thumbnail Resource when found.
   *
   * @param videoId UUID of the video whose thumbnail is requested
   * @return ResponseEntity with the thumbnail Resource (200 if found, 404 if not)
   */
  @Operation(summary = "Get video thumbnail", description = "Get the thumbnail of a video")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Thumbnail retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Thumbnail not found")
      })
  @GetMapping("/{videoId}/thumbnail")
  public ResponseEntity<Resource> getThumbnail(
      @Parameter(description = "ID of the video") @PathVariable UUID videoId) {
    log.debug("Getting thumbnail for video: {}", videoId);
    return videoService.getThumbnail(videoId);
  }
}
