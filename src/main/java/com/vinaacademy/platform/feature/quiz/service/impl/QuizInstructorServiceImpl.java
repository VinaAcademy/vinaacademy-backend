package com.vinaacademy.platform.feature.quiz.service.impl;

import com.vinaacademy.platform.exception.NotFoundException;
import com.vinaacademy.platform.exception.ValidationException;
import com.vinaacademy.platform.feature.quiz.dto.QuizDto;
import com.vinaacademy.platform.feature.quiz.dto.QuizSubmissionResultDto;
import com.vinaacademy.platform.feature.quiz.entity.Quiz;
import com.vinaacademy.platform.feature.quiz.entity.QuizSubmission;
import com.vinaacademy.platform.feature.quiz.mapper.QuizMapper;
import com.vinaacademy.platform.feature.quiz.mapper.QuizSubmissionMapper;
import com.vinaacademy.platform.feature.quiz.repository.QuizRepository;
import com.vinaacademy.platform.feature.quiz.repository.QuizSubmissionRepository;
import com.vinaacademy.platform.feature.quiz.service.QuizInstructorService;
import com.vinaacademy.platform.feature.section.entity.Section;
import com.vinaacademy.platform.feature.section.repository.SectionRepository;
import com.vinaacademy.platform.feature.user.auth.annotation.RequiresResourcePermission;
import com.vinaacademy.platform.feature.user.auth.helpers.SecurityHelper;
import com.vinaacademy.platform.feature.user.constant.ResourceConstants;
import com.vinaacademy.platform.feature.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizInstructorServiceImpl implements QuizInstructorService {
    private final QuizRepository quizRepository;
    private final SectionRepository sectionRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;

    @Autowired
    private SecurityHelper securityHelper;

    @Override
    @Transactional(readOnly = true)
    @RequiresResourcePermission(resourceType = ResourceConstants.LESSON,
            permission = ResourceConstants.VIEW_OWN)
    public QuizDto getQuizByIdForInstructor(UUID id) {
        User user = securityHelper.getCurrentUser();
        if (user == null) {
            throw new ValidationException("User not found");
        }

        Quiz quiz = findQuizById(id);
        if (quiz.getSection() == null) {
            throw new ValidationException("Quiz does not belong to any section");
        }

        return QuizMapper.INSTANCE.quizToQuizDto(quiz);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizDto> getQuizzesByCourseId(UUID courseId) {
        List<Quiz> quizzes = quizRepository.findByCourseId(courseId);
        return quizzes.stream()
                .map(QuizMapper.INSTANCE::quizToQuizDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizDto> getQuizzesBySectionId(UUID sectionId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new NotFoundException("Section not found with id: " + sectionId));

        List<Quiz> quizzes = quizRepository.findBySectionOrderByOrderIndex(section);
        return quizzes.stream()
                .map(QuizMapper.INSTANCE::quizToQuizDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @RequiresResourcePermission(resourceType = ResourceConstants.LESSON)
    public List<QuizSubmissionResultDto> getQuizSubmissions(UUID quizId) {
        List<QuizSubmission> submissions = quizSubmissionRepository
                .findByQuizIdAndUserIdOrderByCreatedDateDesc(quizId, null);

        return submissions.stream()
                .map(QuizSubmissionMapper.INSTANCE::toSubmissionResultDto)
                .collect(Collectors.toList());
    }

    /**
     * Helper method to find a quiz by ID
     */
    private Quiz findQuizById(UUID id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Quiz not found with id: " + id));
    }
}
