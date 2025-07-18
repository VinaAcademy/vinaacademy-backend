package com.vinaacademy.platform.feature.lesson;

import com.vinaacademy.platform.feature.common.response.ApiResponse;
import com.vinaacademy.platform.feature.lesson.dto.LessonProgressDto;
import com.vinaacademy.platform.feature.lesson.service.LessonProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/lesson-progress")
@Slf4j
@RequiredArgsConstructor
public class LessonProgressController {
    private final LessonProgressService lessonProgressService;

    @GetMapping("/{courseId}")
    public ApiResponse<List<LessonProgressDto>> getAllLessonProgressByCourse(@PathVariable UUID courseId) {
        log.info("Fetching all lesson progress for course ID: {}", courseId);
        List<LessonProgressDto> lessonProgressDtos = lessonProgressService.getAllLessonProgressByCourse(courseId);
        return ApiResponse.success(lessonProgressDtos);
    }
}
