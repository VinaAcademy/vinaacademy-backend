package com.vinaacademy.platform.feature.instructor.controller;

import com.vinaacademy.platform.feature.common.response.ApiResponse;

import com.vinaacademy.platform.feature.user.auth.annotation.HasAnyRole;
import com.vinaacademy.platform.feature.user.constant.AuthConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import com.vinaacademy.platform.feature.instructor.dto.InstructorInfoDto;
import com.vinaacademy.platform.feature.instructor.service.InstructorService;

@RestController
@RequestMapping("/api/v1/instructor")
@RequiredArgsConstructor
@Tag(name = "Instructor", description = "Instructor public APIs")
public class InstructorController {

    private final InstructorService instructorService;

    @GetMapping("/{instructorId}")
    public ApiResponse<InstructorInfoDto> getInstructorById(@PathVariable UUID instructorId) {
        InstructorInfoDto instructor = instructorService.getInstructorInfo(instructorId);
        return ApiResponse.success(instructor);
    }

    @PostMapping("/register")
    @HasAnyRole({AuthConstants.STUDENT_ROLE})
    @Operation(summary = "Đăng ký trở thành giảng viên", description = "Học viên đăng ký trở thành giảng viên")
    public ApiResponse<InstructorInfoDto> registerAsInstructor() {
        InstructorInfoDto instructor = instructorService.registerAsInstructor();
        return ApiResponse.success("Đăng ký trở thành giảng viên thành công", instructor);
    }

//    @GetMapping("/check/{userId}")
//    @Operation(summary = "Kiểm tra người dùng có phải là giảng viên",
//            description = "Kiểm tra xem một người dùng có role giảng viên hay không")
//    public ApiResponse<Boolean> checkIfUserIsInstructor(@PathVariable UUID userId) {
//        boolean isInstructor = instructorService.isInstructor(userId);
//        return ApiResponse.success(isInstructor);
//    }
}