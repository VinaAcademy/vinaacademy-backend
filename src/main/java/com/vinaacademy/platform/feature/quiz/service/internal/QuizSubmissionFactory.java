package com.vinaacademy.platform.feature.quiz.service.internal;

import com.vinaacademy.platform.feature.quiz.entity.Quiz;
import com.vinaacademy.platform.feature.quiz.entity.QuizSubmission;
import com.vinaacademy.platform.feature.quiz.entity.UserAnswer;
import com.vinaacademy.platform.feature.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public interface QuizSubmissionFactory {
    QuizSubmission createSubmission(Quiz quiz, User user, List<UserAnswer> gradedAnswers, LocalDateTime start, LocalDateTime end);
}
