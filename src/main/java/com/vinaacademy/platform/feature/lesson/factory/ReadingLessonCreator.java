package com.vinaacademy.platform.feature.lesson.factory;

import com.vinaacademy.platform.exception.ValidationException;
import com.vinaacademy.platform.feature.lesson.dto.LessonRequest;
import com.vinaacademy.platform.feature.lesson.entity.Lesson;
import com.vinaacademy.platform.feature.reading.Reading;
import com.vinaacademy.platform.feature.reading.repository.ReadingRepository;
import com.vinaacademy.platform.feature.section.entity.Section;
import com.vinaacademy.platform.feature.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Concrete creator for creating Reading lessons
 */
@Component
@RequiredArgsConstructor
public class ReadingLessonCreator extends LessonCreator {

    private final ReadingRepository readingRepository;
    
    @Override
    public Lesson createLesson(String title, Section section, User author, boolean isFree, int orderIndex) {
        Reading reading = Reading.builder()
                .title(title)
                .section(section)
                .free(isFree)
                .orderIndex(orderIndex)
                .author(author)
                .content("")
                .build();
        
        return readingRepository.save(reading);
    }
    
    @Override
    public Lesson createLesson(LessonRequest request, Section section, User author) {
        validateRequest(request);
        
        Reading reading = Reading.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .section(section)
                .free(request.isFree())
                .orderIndex(request.getOrderIndex())
                .author(author)
                .content(request.getContent())
                .build();
        
        return readingRepository.save(reading);
    }
    
    @Override
    public Lesson updateLesson(Lesson lesson, LessonRequest request) {
        validateUpdateRequest(request);
        
        if (!(lesson instanceof Reading reading)) {
            throw new ValidationException("Cannot update a non-Reading lesson with Reading data");
        }
        
        reading.setContent(request.getContent());
        
        return readingRepository.save(reading);
    }
    
    @Override
    protected void validateRequest(LessonRequest request) {
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new ValidationException("Content is required for reading lessons");
        }
    }
    
    @Override
    protected void validateUpdateRequest(LessonRequest request) {
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new ValidationException("Content is required for reading lessons");
        }
    }
}