package com.vinaacademy.platform.feature.lesson.factory;

import com.vinaacademy.platform.feature.lesson.dto.LessonRequest;
import com.vinaacademy.platform.feature.lesson.entity.Lesson;
import com.vinaacademy.platform.feature.section.entity.Section;
import com.vinaacademy.platform.feature.user.entity.User;

/**
 * Abstract creator class in the Factory Method pattern 
 * for creating different types of lessons
 */
public abstract class LessonCreator {
    
    /**
     * The factory method that concrete creators must implement
     * to create specific lesson types
     * 
     * @param title The title of the lesson
     * @param section The section this lesson belongs to
     * @param author The user who creates the lesson
     * @param isFree Indicates if the lesson is free to access
     * @param orderIndex The display order of the lesson within its section
     * @return A newly created lesson of the appropriate type
     */
    public abstract Lesson createLesson(String title, Section section, User author, 
                                       boolean isFree, int orderIndex);
    
    /**
     * Factory method that creates a lesson from a request object
     * 
     * @param request The lesson request containing lesson properties
     * @param section The section this lesson belongs to
     * @param author The user who creates the lesson
     * @return A newly created lesson of the appropriate type
     */
    public abstract Lesson createLesson(LessonRequest request, Section section, User author);
    
    /**
     * Validates that the request contains all required 
     * fields for the specific lesson type
     * 
     * @param request The lesson request to validate
     */
    protected abstract void validateRequest(LessonRequest request);
    
    /**
     * Updates an existing lesson with new data from a request
     * 
     * @param lesson The existing lesson to update
     * @param request The request containing the updated data
     * @return The updated lesson entity
     */
    public abstract Lesson updateLesson(Lesson lesson, LessonRequest request);
    
    /**
     * Validates that the request contains all required fields
     * for updating the specific lesson type
     * 
     * @param request The lesson request to validate
     */
    protected abstract void validateUpdateRequest(LessonRequest request);
}