package com.vinaacademy.platform.feature.quiz.service.student.impl;

import com.vinaacademy.platform.exception.BadRequestException;
import com.vinaacademy.platform.exception.NotFoundException;
import com.vinaacademy.platform.exception.ValidationException;
import com.vinaacademy.platform.feature.quiz.dto.QuizDto;
import com.vinaacademy.platform.feature.quiz.dto.QuizSubmissionRequest;
import com.vinaacademy.platform.feature.quiz.dto.QuizSubmissionResultDto;
import com.vinaacademy.platform.feature.quiz.dto.UserAnswerRequest;
import com.vinaacademy.platform.feature.quiz.entity.*;
import com.vinaacademy.platform.feature.quiz.mapper.QuizMapper;
import com.vinaacademy.platform.feature.quiz.mapper.QuizSubmissionMapper;
import com.vinaacademy.platform.feature.quiz.repository.*;
import com.vinaacademy.platform.feature.quiz.service.QuizCacheService;
import com.vinaacademy.platform.feature.quiz.service.student.QuizStudentService;
import com.vinaacademy.platform.feature.quiz.service.student.internal.QuizGradingService;
import com.vinaacademy.platform.feature.quiz.service.student.internal.QuizSessionHandler;
import com.vinaacademy.platform.feature.quiz.service.student.internal.QuizSubmissionFactory;
import com.vinaacademy.platform.feature.quiz.service.student.internal.QuizValidator;
import com.vinaacademy.platform.feature.user.auth.annotation.RequiresResourcePermission;
import com.vinaacademy.platform.feature.user.auth.helpers.SecurityHelper;
import com.vinaacademy.platform.feature.user.constant.ResourceConstants;
import com.vinaacademy.platform.feature.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizStudentServiceImpl implements QuizStudentService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final QuizSessionRepository quizSessionRepository;
    private final QuizCacheService quizCacheService;
    private final SecurityHelper securityHelper;

    // Injected refactored services
    private final QuizValidator quizValidator;
    private final QuizGradingService quizGradingService;
    private final QuizSessionHandler quizSessionHandler;
    private final QuizSubmissionFactory quizSubmissionFactory;

    @Override
    @Transactional(readOnly = true)
    @RequiresResourcePermission(resourceType = ResourceConstants.LESSON)
    public QuizDto getQuizForStudent(UUID id) {
        User user = securityHelper.getCurrentUser();
        if (user == null) {
            throw new ValidationException("User not found");
        }

        Quiz quiz = findQuizById(id);
        if (quiz.isRandomizeQuestions()) {
            // Randomize questions for the quiz
            List<Question> questions = questionRepository.findByQuizOrderByCreatedDate(quiz);
            Collections.shuffle(questions);
            quiz.setQuestions(questions);
        }

        List<UUID> questionIds = quiz.getQuestions().stream()
                .map(Question::getId)
                .collect(Collectors.toList());
        List<Answer> allAnswers = answerRepository.findByQuestionIdIn(questionIds);

        Map<UUID, List<Answer>> answersByQuestionId = allAnswers.stream()
                .collect(Collectors.groupingBy(a -> a.getQuestion().getId()));
        for (Question question : quiz.getQuestions()) {
            List<Answer> answers = answersByQuestionId.getOrDefault(question.getId(),
                    new ArrayList<>());
            Collections.shuffle(answers);
            question.setAnswers(answers);
        }

        return QuizMapper.INSTANCE.quizToQuizDtoHideCorrectAnswers(quiz);
    }

    @Override
    @Transactional
    @RequiresResourcePermission(resourceType = ResourceConstants.LESSON,
            permission = ResourceConstants.VIEW,
            idParam = "request.quizId")
    public QuizSubmissionResultDto submitQuiz(QuizSubmissionRequest request) {
        User currentUser = securityHelper.getCurrentUser();
        Quiz quiz = findQuizById(request.getQuizId());

        // Validate submission using the validator service
        quizValidator.validateRetakePolicy(quiz, currentUser.getId());

        // Get current time as end time
        LocalDateTime endTime = LocalDateTime.now();

        // Find and validate the active session
        QuizSession session = quizValidator.validateActiveSession(request.getQuizId(), currentUser.getId());
        LocalDateTime startTime = session.getStartTime();

        // Validate time limit if quiz has one
        quizValidator.validateTimeLimit(quiz, session, startTime, endTime);

        // Process quiz submission using refactored services
        return processSubmitQuiz(request, session, startTime, endTime);
    }

    private QuizSubmissionResultDto processSubmitQuiz(QuizSubmissionRequest request, QuizSession quizSession,
                                                      LocalDateTime startTime, LocalDateTime endTime) {
        Quiz quiz = quizSession.getQuiz();
        User currentUser = quizSession.getUser();

        // Grade all answers using the grading service
        List<UserAnswer> gradedAnswers = request.getAnswers().stream()
                .map(answerRequest -> quizGradingService.gradeAnswer(
                        findQuestionById(answerRequest.getQuestionId()), answerRequest))
                .collect(Collectors.toList());

        // Create submission using the factory
        QuizSubmission submission = quizSubmissionFactory.createSubmission(
                quiz, currentUser, gradedAnswers, startTime, endTime);

        // Link submission to session
        submission.setQuizSession(quizSession);
        QuizSubmission savedSubmission = quizSubmissionRepository.save(submission);

        // Deactivate session
        quizSession.setQuizSubmission(submission);
        quizSession.setActive(false);
        quizSessionRepository.save(quizSession);

        // Convert to result DTO
        return QuizSubmissionMapper.INSTANCE.toSubmissionResultDto(savedSubmission);
    }


    @Override
    @Transactional
    @RequiresResourcePermission(resourceType = ResourceConstants.LESSON,
            permission = ResourceConstants.VIEW)
    public QuizSession startQuiz(UUID quizId) {
        User currentUser = securityHelper.getCurrentUser();
        Quiz quiz = findQuizById(quizId);

        // Check if there's an active session already using the session handler
        Optional<QuizSession> existingSession = quizSessionHandler.findActiveSession(quizId, currentUser.getId());

        if (existingSession.isPresent()) {
            QuizSession session = existingSession.get();

            // If the session has expired but is still marked active, deactivate it
            if (session.isExpired()) {
                quizSessionHandler.deactivateSession(session);
            } else {
                // Return the existing active session
                return session;
            }
        }

        // Create a new session
        QuizSession session = QuizSession.createNewSession(quiz, currentUser);
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(quiz.getTimeLimit());
        session.setExpiryTime(expiryTime);

        QuizSession savedSession = quizSessionRepository.save(session);

        // Schedule session expiry using the session handler
        quizSessionHandler.scheduleSessionExpiry(savedSession);

        return savedSession;
    }


    @Override
    @Transactional(readOnly = true)
    @RequiresResourcePermission(resourceType = ResourceConstants.LESSON,
            permission = ResourceConstants.VIEW)
    public QuizSubmissionResultDto getLatestSubmission(UUID quizId) {
        User currentUser = securityHelper.getCurrentUser();

        Optional<QuizSubmission> latestSubmission = quizSubmissionRepository
                .findFirstByQuizIdAndUserIdOrderByCreatedDateDesc(quizId, currentUser.getId());

        return latestSubmission.map(QuizSubmissionMapper.INSTANCE::toSubmissionResultDto)
                .orElse(null);

    }

    @Override
    @Transactional(readOnly = true)
    @RequiresResourcePermission(resourceType = ResourceConstants.LESSON,
            permission = ResourceConstants.VIEW)
    public List<QuizSubmissionResultDto> getSubmissionHistory(UUID quizId) {
        User currentUser = securityHelper.getCurrentUser();

        List<QuizSubmission> submissions = quizSubmissionRepository
                .findByQuizIdAndUserIdOrderByCreatedDateDesc(quizId, currentUser.getId());

        if (submissions.isEmpty()) {
            throw new NotFoundException("No submissions found for quiz: " + quizId);
        }

        return submissions.stream()
                .map(QuizSubmissionMapper.INSTANCE::toSubmissionResultDto)
                .collect(Collectors.toList());
    }


    @Transactional
    @Override
    public void cacheQuizAnswer(UUID quizId, UserAnswerRequest request) {
        User currentUser = securityHelper.getCurrentUser();

        Optional<QuizSession> existingSession = quizSessionHandler.findActiveSession(quizId, currentUser.getId());
        if (existingSession.isEmpty()) {
            throw BadRequestException.message("Không có phiên làm bài nào đang hoạt động");
        }

        QuizSession session = existingSession.get();
        if (session.isExpired()) {
            quizSessionHandler.deactivateSession(session);
            throw BadRequestException.message("Phiên làm bài đã hết hạn");
        }

        // add the question type to the request
        Question question = findQuestionById(request.getQuestionId());
        request.setQuestionType(question.getQuestionType());

        // Cache the answer using the quiz cache service
        quizCacheService.updateCacheAnswer(currentUser.getId(), session.getId(), quizId, request);
    }

    /**
     * Helper method to find a quiz by ID
     */
    private Quiz findQuizById(UUID id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Quiz not found with id: " + id));
    }

    /**
     * Helper method to find a question by ID
     */
    private Question findQuestionById(UUID id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Question not found with id: " + id));
    }
}
