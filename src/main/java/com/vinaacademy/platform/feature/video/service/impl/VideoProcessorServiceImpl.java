package com.vinaacademy.platform.feature.video.service.impl;

import com.vinaacademy.platform.exception.BadRequestException;
import com.vinaacademy.platform.feature.notification.dto.NotificationCreateDTO;
import com.vinaacademy.platform.feature.notification.service.NotificationService;
import com.vinaacademy.platform.feature.request.ProcessVideoRequest;
import com.vinaacademy.platform.feature.storage.entity.MediaFile;
import com.vinaacademy.platform.feature.storage.properties.StorageProperties;
import com.vinaacademy.platform.feature.storage.repository.MediaFileRepository;
import com.vinaacademy.platform.feature.storage.service.S3Service;
import com.vinaacademy.platform.feature.video.entity.Video;
import com.vinaacademy.platform.feature.video.enums.VideoStatus;
import com.vinaacademy.platform.feature.video.repository.VideoRepository;
import com.vinaacademy.platform.feature.video.service.VideoProcessorService;
import com.vinaacademy.platform.feature.video.utils.FFmpegUtils;
import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoProcessorServiceImpl implements VideoProcessorService {

    private final VideoRepository videoRepository;
    private final MediaFileRepository mediaFileRepository;
    private final NotificationService notificationService;
    private final StorageProperties storageProperties;
    private final S3Service s3Service;

    @Value("${application.url.frontend}")
    private String frontendUrl;

    @Autowired
    @Lazy
    private VideoProcessorService self;

    @Async("videoTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processVideo(UUID videoId, Path inputFile) {
        Path outputDir = Paths.get(storageProperties.getHlsDir(), videoId.toString());
        Path thumbnailPath = Paths.get(storageProperties.getThumbnailDir(), videoId + ".jpg");

        try {
            // Convert to HLS and upload to MinIO
            String s3KeyPrefix = FFmpegUtils.convertToAdaptiveHLSAndUpload(inputFile, outputDir, thumbnailPath, s3Service, videoId);
            
            Video video = videoRepository.findByIdWithLock(videoId)
                    .orElseThrow(() -> BadRequestException.message("Không tìm thấy video"));

            updateVideoSuccess(video, s3KeyPrefix, thumbnailPath, inputFile);
            notifySuccess(video);
            log.debug("✅ Video {} processed successfully.", videoId);
            videoRepository.save(video);

            // delete the original video file after processing
            Files.deleteIfExists(inputFile);
        } catch (Exception e) {
            log.error("❌ Error processing video {}: {}", videoId, e.getMessage(), e);
            Video video = videoRepository.findByIdWithLock(videoId)
                    .orElseThrow(() -> BadRequestException.message("Không tìm thấy video"));
            updateVideoFailure(video);
            notifyFailure(video, e.getMessage());
            videoRepository.save(video);
        }
    }

    private void updateVideoSuccess(Video video, String s3KeyPrefix, Path thumbnailPath, Path inputFile) throws IOException, InterruptedException {
        video.setStatus(VideoStatus.READY);
        // Store S3 key prefix instead of local path
        video.setHlsPath(s3KeyPrefix);
        video.setDuration(FFmpegUtils.getVideoDurationInSeconds(inputFile));
        if (video.getThumbnailUrl() == null) {
            // Store MinIO thumbnail URL
            String thumbnailKey = "videos/thumbnails/" + video.getId().toString() + ".jpg";
            video.setThumbnailUrl(thumbnailKey);
        }
    }

    private void updateVideoFailure(Video video) {
        video.setStatus(VideoStatus.ERROR);
    }

    private void notifySuccess(Video video) {
        String courseId = video.getSection().getCourse().getId().toString();
        notificationService.createNotification(NotificationCreateDTO.builder()
                .title("Video " + video.getTitle() + " đã được xử lý thành công")
                .content("Bạn có thể xem tại đây.")
                .targetUrl(frontendUrl + "/instructor/courses/" + courseId + "/lectures/" + video.getId())
                .userId(video.getAuthor().getId())
                .build());
    }

    private void notifyFailure(Video video, String errorMessage) {
        String courseId = video.getSection().getCourse().getId().toString();
        notificationService.createNotification(NotificationCreateDTO.builder()
                .title("Lỗi xử lý video " + video.getTitle())
                .content("Có lỗi xảy ra: " + errorMessage)
                .targetUrl(frontendUrl + "/instructor/courses/" + courseId + "/lectures/" + video.getId())
                .userId(video.getAuthor().getId())
                .build());
    }

    @Transactional
    public void processVideo(@Valid ProcessVideoRequest processVideoRequest) {
        Video video = videoRepository.findByIdWithLock(processVideoRequest.getVideoId())
                .orElseThrow(() -> BadRequestException.message("Không tìm thấy video"));
        if (VideoStatus.PROCESSING.equals(video.getStatus())) {
            log.warn("Video {} is already being processed", video.getId());
            throw BadRequestException.message("Video đang được xử lý");
        }
        MediaFile mediaFile = mediaFileRepository.findById(processVideoRequest.getMediaFileId())
                .orElseThrow(() -> BadRequestException.message("Không tìm thấy tệp video"));
        String videoPath = mediaFile.getFilePath();
        Path inputFile = Paths.get(videoPath);
        if (!Files.exists(inputFile)) {
            log.error("Video file does not exist: {}", videoPath);
            throw BadRequestException.message("Tệp video không tồn tại");
        }
        log.debug("Starting video processing for video: {}", video.getId());
        video.setStatus(VideoStatus.PROCESSING);
        videoRepository.save(video);
        self.processVideo(video.getId(), inputFile);
    }
}
