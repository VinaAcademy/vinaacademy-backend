package com.vinaacademy.platform.feature.video.service;

import com.vinaacademy.platform.feature.request.ProcessVideoRequest;
import com.vinaacademy.platform.feature.video.dto.VideoDto;
import com.vinaacademy.platform.feature.video.dto.VideoRequest;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.UUID;

public interface VideoService {
    VideoDto uploadVideo(MultipartFile file, VideoRequest videoRequest) throws IOException;

    /**
     * Get a video segment resource for streaming
     *
     * @param videoId the video ID
     * @param subPath the path to the segment relative to the video directory
     * @return ResponseEntity containing the video segment resource
     * @throws MalformedURLException if the path is invalid
     */
    ResponseEntity<Resource> getVideoSegment(UUID videoId, String subPath) throws MalformedURLException;

    ResponseEntity<Resource> getThumbnail(UUID videoId);
}
