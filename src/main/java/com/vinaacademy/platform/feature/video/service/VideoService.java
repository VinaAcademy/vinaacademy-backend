package com.vinaacademy.platform.feature.video.service;

import com.vinaacademy.platform.feature.video.dto.VideoDto;
import com.vinaacademy.platform.feature.video.dto.VideoRequest;
import java.io.IOException;
import java.util.UUID;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface VideoService {
  VideoDto uploadVideo(MultipartFile file, VideoRequest videoRequest) throws IOException;

  ResponseEntity<Resource> getThumbnail(UUID videoId);

  /**
   * Get a direct streaming URL (presigned URL) for direct access to MinIO
   *
   * @param videoId the video ID
   * @param subPath the path to the segment relative to the video directory
   * @return presigned URL for direct streaming
   */
  String getSegmentStreaming(UUID videoId, String subPath);

  ByteArrayResource getRewriteManifestProxy(UUID videoId, String basePath, String subPath)
      throws IOException;
}
