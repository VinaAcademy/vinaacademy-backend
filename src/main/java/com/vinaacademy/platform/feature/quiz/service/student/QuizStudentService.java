package com.vinaacademy.platform.feature.quiz.service.student;

import com.vinaacademy.platform.feature.quiz.dto.QuizDto;
import com.vinaacademy.platform.feature.quiz.dto.QuizSubmissionRequest;
import com.vinaacademy.platform.feature.quiz.dto.QuizSubmissionResultDto;
import com.vinaacademy.platform.feature.quiz.dto.UserAnswerRequest;
import com.vinaacademy.platform.feature.quiz.entity.QuizSession;

import java.util.List;
import java.util.UUID;

public interface QuizStudentService {
    QuizDto getQuizForStudent(UUID id);

    QuizSubmissionResultDto submitQuiz(QuizSubmissionRequest request);

    QuizSession startQuiz(UUID quizId);

    QuizSubmissionResultDto getLatestSubmission(UUID quizId);

    List<QuizSubmissionResultDto> getSubmissionHistory(UUID quizId);

    void cacheQuizAnswer(UUID quizId, UserAnswerRequest request);

}
