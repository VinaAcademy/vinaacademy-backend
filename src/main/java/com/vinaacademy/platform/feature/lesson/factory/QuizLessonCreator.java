package com.vinaacademy.platform.feature.lesson.factory;

import com.vinaacademy.platform.exception.ValidationException;
import com.vinaacademy.platform.feature.lesson.dto.LessonRequest;
import com.vinaacademy.platform.feature.lesson.entity.Lesson;
import com.vinaacademy.platform.feature.quiz.entity.Quiz;
import com.vinaacademy.platform.feature.quiz.repository.QuizRepository;
import com.vinaacademy.platform.feature.section.entity.Section;
import com.vinaacademy.platform.feature.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Concrete creator for creating Quiz lessons
 */
@Component
@RequiredArgsConstructor
public class QuizLessonCreator extends LessonCreator {

    private final QuizRepository quizRepository;
    
    @Override
    public Lesson createLesson(String title, Section section, User author, boolean isFree, int orderIndex) {
        Quiz quiz = Quiz.builder()
                .title(title)
                .section(section)
                .free(isFree)
                .orderIndex(orderIndex)
                .author(author)
                .passingScore(0.0)
                .totalPoints(0.0)
                .duration(30)
                .build();
        
        return quizRepository.save(quiz);
    }
    
    @Override
    public Lesson createLesson(LessonRequest request, Section section, User author) {
//        validateRequest(request);
        
        Quiz quiz = Quiz.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .section(section)
                .free(request.isFree())
                .orderIndex(request.getOrderIndex())
                .author(author)
//                .passingScore(request.getPassPoint())
//                .totalPoints(request.getTotalPoint())
//                .duration(request.getDuration())
                .build();
        
        // Apply quiz settings if provided
        applyQuizSettings(quiz, request.getSettings());
        
        return quizRepository.save(quiz);
    }
    
    @Override
    public Lesson updateLesson(Lesson lesson, LessonRequest request) {
//        validateUpdateRequest(request);
        
        if (!(lesson instanceof Quiz quiz)) {
            throw new ValidationException("Cannot update a non-Quiz lesson with Quiz data");
        }
        
        quiz.setPassingScore(request.getPassPoint());
        quiz.setTotalPoints(request.getTotalPoint());
//        quiz.setDuration(request.getDuration());
        
        // Apply quiz settings if provided
        applyQuizSettings(quiz, request.getSettings());
        
        return quizRepository.save(quiz);
    }
    
    /**
     * Apply quiz settings from the request to the quiz entity
     * 
     * @param quiz The quiz entity
     * @param settings The settings map from the request
     */
    private void applyQuizSettings(Quiz quiz, Map<String, Object> settings) {
        if (settings == null || settings.isEmpty()) {
            return;
        }
        
        // Apply randomize questions setting
        if (settings.containsKey("randomizeQuestions")) {
            quiz.setRandomizeQuestions(Boolean.parseBoolean(settings.get("randomizeQuestions").toString()));
        }
        
        // Apply show correct answers setting
        if (settings.containsKey("showCorrectAnswers")) {
            quiz.setShowCorrectAnswers(Boolean.parseBoolean(settings.get("showCorrectAnswers").toString()));
        }
        
        // Apply allow retake setting
        if (settings.containsKey("allowRetake")) {
            quiz.setAllowRetake(Boolean.parseBoolean(settings.get("allowRetake").toString()));
        }
        
        // Apply require passing score setting
        if (settings.containsKey("requirePassingScore")) {
            quiz.setRequirePassingScore(Boolean.parseBoolean(settings.get("requirePassingScore").toString()));
        }
        
        // Apply passing score setting
        if (settings.containsKey("passingScore")) {
            double passingScore = Double.parseDouble(settings.get("passingScore").toString());
            if (passingScore < 0 || passingScore > 100) {
                throw new ValidationException("Passing score must be between 0 and 100");
            }
            quiz.setPassingScore(passingScore);
        }
        
        // Apply time limit setting
        if (settings.containsKey("timeLimit")) {
            int timeLimit = Integer.parseInt(settings.get("timeLimit").toString());
            if (timeLimit < 0) {
                throw new ValidationException("Time limit cannot be negative");
            }
            quiz.setTimeLimit(timeLimit);
        }
    }
    
    @Override
    protected void validateRequest(LessonRequest request) {
        if (request.getPassPoint() == null || request.getTotalPoint() == null || request.getDuration() == null) {
            throw new ValidationException("Pass point, total point, and duration are required for quiz lessons");
        }

        if (request.getPassPoint() < 0 || request.getTotalPoint() < 0) {
            throw new ValidationException("Points cannot be negative");
        }

        if (request.getPassPoint() > request.getTotalPoint()) {
            throw new ValidationException("Pass point cannot be greater than total point");
        }

        if (request.getDuration() <= 0) {
            throw new ValidationException("Duration must be positive");
        }
    }
    
    @Override
    protected void validateUpdateRequest(LessonRequest request) {
        if (request.getPassPoint() == null || request.getTotalPoint() == null || request.getDuration() == null) {
            throw new ValidationException("Pass point, total point, and duration are required for quiz lessons");
        }

        if (request.getPassPoint() < 0 || request.getTotalPoint() < 0) {
            throw new ValidationException("Points cannot be negative");
        }

        if (request.getPassPoint() > request.getTotalPoint()) {
            throw new ValidationException("Pass point cannot be greater than total point");
        }

        if (request.getDuration() <= 0) {
            throw new ValidationException("Duration must be positive");
        }
    }
}