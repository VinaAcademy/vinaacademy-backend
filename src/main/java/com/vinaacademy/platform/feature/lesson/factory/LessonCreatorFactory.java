package com.vinaacademy.platform.feature.lesson.factory;

import com.vinaacademy.platform.exception.ValidationException;
import com.vinaacademy.platform.feature.course.enums.LessonType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Factory for selecting the appropriate LessonCreator based on lesson type
 */
@Component
@RequiredArgsConstructor
public class LessonCreatorFactory {

    private final VideoLessonCreator videoLessonCreator;
    private final ReadingLessonCreator readingLessonCreator;
    private final QuizLessonCreator quizLessonCreator;
    
    /**
     * Returns the appropriate creator based on the lesson type
     * 
     * @param type The type of lesson to create
     * @return The appropriate LessonCreator
     * @throws ValidationException if an unsupported lesson type is provided
     */
    public LessonCreator getCreator(LessonType type) {
        return switch (type) {
            case VIDEO -> videoLessonCreator;
            case READING -> readingLessonCreator;
            case QUIZ -> quizLessonCreator;
            default -> throw new ValidationException("Unsupported lesson type: " + type);
        };
    }
}