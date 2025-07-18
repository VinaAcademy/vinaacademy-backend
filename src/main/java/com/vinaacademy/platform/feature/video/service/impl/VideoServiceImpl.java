package com.vinaacademy.platform.feature.video.service.impl;

import com.vinaacademy.platform.exception.BadRequestException;
import com.vinaacademy.platform.feature.lesson.repository.LessonRepository;
import com.vinaacademy.platform.feature.lesson.repository.projection.LessonAccessInfoDto;
import com.vinaacademy.platform.feature.storage.dto.MediaFileDto;
import com.vinaacademy.platform.feature.storage.enums.FileType;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Slf4j
public class VideoServiceImpl implements VideoService {
    @Autowired
    private VideoRepository videoRepository;
    @Autowired
    private LessonRepository lessonRepository;
    @Autowired
    private VideoProcessorService videoProcessorService;
    @Autowired
    private StorageService storageService;

    @Autowired
    private VideoValidator videoValidator;
    @Autowired
    private SecurityHelper securityHelper;


    @Override
    public VideoDto uploadVideo(MultipartFile file, VideoRequest videoRequest) throws IOException {
        Video video = videoRepository.findByIdWithLock(videoRequest.getLessonId())
                .orElseThrow(() -> BadRequestException.message("Không tìm thấy bài học"));
        videoValidator.validate(file);

        User currentUser = securityHelper.getCurrentUser();

        LessonAccessInfoDto lessonAccessInfo = lessonRepository
                .getLessonAccessInfo(videoRequest.getLessonId(),
                        currentUser.getId())
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
    public ResponseEntity<Resource> getVideoSegment(UUID videoId, String subPath) throws MalformedURLException {
        log.debug("Getting video segment: {}/{}", videoId, subPath);

        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> BadRequestException.message("Không tìm thấy video"));

        Path segmentPath = Paths.get(video.getHlsPath(), subPath);

        if (!Files.exists(segmentPath)) {
            log.warn("Video segment not found: {}/{}", videoId, subPath);
            return ResponseEntity.notFound().build();
        }

        String filename = segmentPath.getFileName().toString();
        String contentType;
        if (filename.endsWith(".m3u8")) {
            contentType = "application/x-mpegURL";
        } else if (filename.endsWith(".ts")) {
            contentType = "video/MP2T";
        } else {
            contentType = "application/octet-stream";
        }

        Resource resource = new UrlResource(segmentPath.toUri());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
    }

    @Override
    public ResponseEntity<Resource> getThumbnail(UUID videoId) {
        log.debug("Getting thumbnail for video: {}", videoId);

        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> BadRequestException.message("Không tìm thấy video"));

        if (video.getThumbnailUrl() == null || video.getThumbnailUrl().isEmpty()) {
            log.warn("Thumbnail URL not set for video: {}", videoId);
            throw BadRequestException.message("Thumbnail URL not set");
        }

        String url = video.getThumbnailUrl();

        try {
            Resource resource;
            String filename;
            String contentType;

            // ✅ Case 1: External URL
            if (url.startsWith("http://") || url.startsWith("https://")) {
                resource = new UrlResource(url);
                if (!resource.exists() || !resource.isReadable()) {
                    throw BadRequestException.message("Thumbnail URL is not accessible");
                }

                filename = Paths.get(new URI(url).getPath()).getFileName().toString();
            }
            // ✅ Case 2: Local file path
            else {
                Path thumbnailPath = Paths.get(url);
                if (!Files.exists(thumbnailPath)) {
                    log.warn("Thumbnail not found: {}", url);
                    throw BadRequestException.message("Thumbnail not found");
                }

                resource = new UrlResource(thumbnailPath.toUri());
                filename = thumbnailPath.getFileName().toString();
            }

            // ✅ Guess content type
            contentType = URLConnection.guessContentTypeFromName(filename);
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (MalformedURLException | URISyntaxException e) {
            log.error("Error serving thumbnail: {}", e.getMessage());
            throw BadRequestException.message("Error serving thumbnail");
        }
    }

}
