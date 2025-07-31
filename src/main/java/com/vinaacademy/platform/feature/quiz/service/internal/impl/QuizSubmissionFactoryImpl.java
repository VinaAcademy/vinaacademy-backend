package com.vinaacademy.platform.feature.quiz.service.internal.impl;

import com.vinaacademy.platform.feature.course.repository.UserProgressRepository;
import com.vinaacademy.platform.feature.lesson.entity.UserProgress;
import com.vinaacademy.platform.feature.lesson.service.LessonService;
import com.vinaacademy.platform.feature.quiz.entity.Quiz;
import com.vinaacademy.platform.feature.quiz.entity.QuizSubmission;
import com.vinaacademy.platform.feature.quiz.entity.UserAnswer;
import com.vinaacademy.platform.feature.quiz.service.internal.QuizSubmissionFactory;
import com.vinaacademy.platform.feature.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuizSubmissionFactoryImpl implements QuizSubmissionFactory {

    private final UserProgressRepository userProgressRepository;
    private final LessonService lessonService;

    @Override
    public QuizSubmission createSubmission(Quiz quiz, User user, List<UserAnswer> gradedAnswers, 
                                         LocalDateTime start, LocalDateTime end) {
        QuizSubmission submission = QuizSubmission.builder()
                .startTime(start)
                .endTime(end)
                .build();

        // Add graded answers to submission
        gradedAnswers.forEach(answer -> {
            answer.setSubmission(submission);
            submission.addUserAnswer(answer);
        });

        // Calculate total earned points
        double earnedPoints = gradedAnswers.stream()
                .mapToDouble(UserAnswer::getEarnedPoints)
                .sum();

        // Set submission details
        double totalPoints = quiz.getTotalPoints();
        submission.setTotalPoints(totalPoints);
        submission.setScore(Double.parseDouble(new DecimalFormat("#.0").format(earnedPoints)));

        // Determine pass status
        double scorePercentage = (totalPoints > 0) ? (earnedPoints / totalPoints) * 100 : 0;
        submission.setPassed(scorePercentage >= quiz.getPassingScore());

        // Mark lesson as completed if passed
        if (submission.isPassed()) {
            Optional<UserProgress> existingProgress = userProgressRepository
                    .findByLessonIdAndUserId(quiz.getId(), user.getId());
            
            if (existingProgress.isEmpty()) {
                lessonService.markLessonCompleted(quiz, user);
            }
        }

        return submission;
    }
}
