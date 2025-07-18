package com.vinaacademy.platform.feature.quiz.service;

import com.vinaacademy.platform.feature.quiz.dto.*;
import com.vinaacademy.platform.feature.quiz.entity.QuizSession;

import java.util.List;
import java.util.UUID;

public interface QuizService {
    /**
     * Get a quiz by ID
     */
    QuizDto getQuizByIdForInstructor(UUID id);
    
    /**
     * Get a quiz for student view (hiding correct answers)
     */
    QuizDto getQuizForStudent(UUID id);
    
    /**
     * Get all quizzes for a course
     */
    List<QuizDto> getQuizzesByCourseId(UUID courseId);
    
    /**
     * Get all quizzes for a section
     */
    List<QuizDto> getQuizzesBySectionId(UUID sectionId);
    
    /**
     * Start a quiz attempt and record the server start time
     */
    QuizSession startQuiz(UUID quizId);
    
    /**
     * Submit a quiz attempt as a student
     */
    QuizSubmissionResultDto submitQuiz(QuizSubmissionRequest request);
    
    /**
     * Get a student's latest submission for a quiz
     */
    QuizSubmissionResultDto getLatestSubmission(UUID quizId);
    
    /**
     * Get all submissions for a quiz by a student
     */
    List<QuizSubmissionResultDto> getSubmissionHistory(UUID quizId);
    
    /**
     * Get all student submissions for a quiz (instructor view)
     */
    List<QuizSubmissionResultDto> getQuizSubmissions(UUID quizId);

    void cacheQuizAnswer(UUID quizId, UserAnswerRequest request);
}
