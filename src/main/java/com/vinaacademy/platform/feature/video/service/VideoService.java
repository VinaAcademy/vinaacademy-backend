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
  /**
 * Uploads a video file with associated metadata and returns the stored video's representation.
 *
 * @param file         the multipart file containing the video bytes to be uploaded
 * @param videoRequest metadata and options for the upload (title, description, visibility, etc.)
 * @return             a VideoDto representing the stored video (IDs, URLs, and persisted metadata)
 * @throws IOException if reading the multipart file or writing the video to storage fails
 */
VideoDto uploadVideo(MultipartFile file, VideoRequest videoRequest) throws IOException;

  /**
 * Retrieves the thumbnail image for the specified video.
 *
 * @param videoId UUID of the video whose thumbnail is requested
 * @return a ResponseEntity containing the thumbnail as a Resource with appropriate HTTP headers and status
 */
ResponseEntity<Resource> getThumbnail(UUID videoId);

  /**
 * Returns a time-limited presigned URL that clients can use to stream a video segment directly from MinIO.
 *
 * @param videoId identifier of the video whose segment is requested
 * @param subPath path to the segment file relative to the video's storage directory (e.g., manifest or .ts segment)
 * @return a presigned URL granting temporary direct access to the requested segment
 */
  String getSegmentStreaming(UUID videoId, String subPath);

  /**
       * Retrieve and return a proxied or rewritten HLS/DASH manifest for a specific video segment.
       *
       * <p>The returned ByteArrayResource contains the manifest bytes after any server-side
       * rewriting (for example, adjusting segment URLs or inserting proxy/presigned links)
       * required for client playback through the platform.</p>
       *
       * @param videoId the UUID of the video whose manifest is requested
       * @param basePath the base directory or storage prefix where the video's manifests reside
       * @param subPath the manifest path relative to {@code basePath} (e.g., playlist or variant path)
       * @return a ByteArrayResource containing the rewritten manifest data
       * @throws IOException if reading from storage or rewriting the manifest fails
       */
      ByteArrayResource getRewriteManifestProxy(UUID videoId, String basePath, String subPath)
      throws IOException;
}
