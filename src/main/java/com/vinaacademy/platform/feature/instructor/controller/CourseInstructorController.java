package com.vinaacademy.platform.feature.instructor.controller;

import com.vinaacademy.platform.feature.common.response.ApiResponse;
import com.vinaacademy.platform.feature.instructor.dto.CourseInstructorDto;
import com.vinaacademy.platform.feature.instructor.dto.CourseInstructorDtoRequest;
import com.vinaacademy.platform.feature.instructor.service.CourseInstructorService;
import com.vinaacademy.platform.feature.user.auth.annotation.HasAnyRole;
import com.vinaacademy.platform.feature.user.constant.AuthConstants;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/courseinstructor")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "InstructorCourse", description = "Quản lý instructor")
public class CourseInstructorController {

    private final CourseInstructorService service;
    
    @HasAnyRole({AuthConstants.INSTRUCTOR_ROLE, AuthConstants.STAFF_ROLE, AuthConstants.ADMIN_ROLE})
    @PostMapping
    public ApiResponse<CourseInstructorDto> create(@RequestBody @Valid CourseInstructorDtoRequest req) {
    	log.debug("tạo instructor cho course "+req.getCourseId());
        CourseInstructorDto dto = service.createCourseInstructor(req);
        
        return ApiResponse.success(dto);
    }
}
