package com.vinaacademy.platform.feature.lesson.service.impl;

import com.vinaacademy.platform.feature.course.repository.UserProgressRepository;
import com.vinaacademy.platform.feature.lesson.dto.LessonProgressDto;
import com.vinaacademy.platform.feature.lesson.mapper.LessonProgressMapper;
import com.vinaacademy.platform.feature.lesson.service.LessonProgressService;
import com.vinaacademy.platform.feature.user.auth.helpers.SecurityHelper;
import com.vinaacademy.platform.feature.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LessonProgressServiceImpl implements LessonProgressService {
    private final UserProgressRepository userProgressRepository;

    @Autowired private SecurityHelper securityHelper;

    @Override
    public List<LessonProgressDto> getAllLessonProgressByCourse(UUID courseId) {
        User user = securityHelper.getCurrentUser();
        return userProgressRepository.findLessonProgressByCourseUser(courseId, user.getId())
                .stream()
                .map(LessonProgressMapper.INSTANCE::toDto)
                .toList();
    }
}
