package com.vinaacademy.platform.feature.quiz.service;

import com.vinaacademy.platform.feature.quiz.dto.QuizDto;
import com.vinaacademy.platform.feature.quiz.dto.QuizSubmissionResultDto;

import java.util.List;
import java.util.UUID;

public interface QuizInstructorService {
    QuizDto getQuizByIdForInstructor(UUID id);

    List<QuizDto> getQuizzesByCourseId(UUID courseId);

    List<QuizDto> getQuizzesBySectionId(UUID sectionId);

    List<QuizSubmissionResultDto> getQuizSubmissions(UUID quizId);

}
