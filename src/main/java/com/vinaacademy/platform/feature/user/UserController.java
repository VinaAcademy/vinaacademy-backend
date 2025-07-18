package com.vinaacademy.platform.feature.user;

import com.vinaacademy.platform.feature.common.response.ApiResponse;
import com.vinaacademy.platform.feature.user.dto.UpdateUserInfoRequest;
import com.vinaacademy.platform.feature.user.dto.UserDto;
import com.vinaacademy.platform.feature.user.dto.UserViewDto;
import com.vinaacademy.platform.feature.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@RequestMapping("/api/v1/users")
@Slf4j
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserDto> getCurrentUser() {
        return ApiResponse.success("Get current user successfully",
                userService.getCurrentUser());
    }
    
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cập nhập thông tin user")
    @PutMapping("/update-info")
    public ApiResponse<UserDto> updateUserInfo(@RequestBody @Valid  UpdateUserInfoRequest request) {
        UserDto updatedUser = userService.updateUserInfo(request);
        log.debug("update info for user "+updatedUser.getId());
        return ApiResponse.success(updatedUser);
    }
    
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Xem thông tin user từ id")
    @GetMapping("/view/{userId}")
    public ApiResponse<UserViewDto> viewUserInfo(@PathVariable UUID userId) {
        UserViewDto viewUser = userService.viewUser(userId);
        log.debug("get view info for user "+viewUser.getId());
        return ApiResponse.success(viewUser);
    }
}
