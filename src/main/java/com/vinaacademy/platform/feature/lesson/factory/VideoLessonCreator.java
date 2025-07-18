package com.vinaacademy.platform.feature.lesson.factory;

import com.vinaacademy.platform.exception.ValidationException;
import com.vinaacademy.platform.feature.lesson.dto.LessonRequest;
import com.vinaacademy.platform.feature.lesson.entity.Lesson;
import com.vinaacademy.platform.feature.section.entity.Section;
import com.vinaacademy.platform.feature.user.entity.User;
import com.vinaacademy.platform.feature.video.entity.Video;
import com.vinaacademy.platform.feature.video.enums.VideoStatus;
import com.vinaacademy.platform.feature.video.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Concrete creator for creating Video lessons
 */
@Component
@RequiredArgsConstructor
public class VideoLessonCreator extends LessonCreator {

    private final VideoRepository videoRepository;

    @Override
    public Lesson createLesson(String title, Section section, User author, boolean isFree, int orderIndex) {
        Video video = Video.builder()
                .title(title)
                .section(section)
                .free(isFree)
                .orderIndex(orderIndex)
                .author(author)
                .status(VideoStatus.NO_VIDEO)
                .build();

        return videoRepository.save(video);
    }
    
    @Override
    public Lesson createLesson(LessonRequest request, Section section, User author) {
        validateRequest(request);
        
        Video video = Video.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .section(section)
                .free(request.isFree())
                .orderIndex(request.getOrderIndex())
                .author(author)
                .thumbnailUrl(request.getThumbnailUrl())
                .status(VideoStatus.NO_VIDEO)
                .build();
        
        return videoRepository.save(video);
    }
    
    @Override
    public Lesson updateLesson(Lesson lesson, LessonRequest request) {
        validateUpdateRequest(request);
        
        if (!(lesson instanceof Video video)) {
            throw new ValidationException("Cannot update a non-Video lesson with Video data");
        }
        
        // Update video-specific fields
        if (request.getThumbnailUrl() != null) {
            video.setThumbnailUrl(request.getThumbnailUrl());
        }
        
        return videoRepository.save(video);
    }
    
    @Override
    protected void validateRequest(LessonRequest request) {
        // Video-specific validations can be added here
        // Currently, no special validation needed for videos
    }
    
    @Override
    protected void validateUpdateRequest(LessonRequest request) {
        // No specific validation needed for video updates
        // But we could add validation for video-specific fields if needed
    }
}