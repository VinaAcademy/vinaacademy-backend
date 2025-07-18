package com.vinaacademy.platform.feature.user.auth;

import com.vinaacademy.platform.feature.common.response.ApiResponse;
import com.vinaacademy.platform.feature.user.auth.dto.*;
import com.vinaacademy.platform.feature.user.auth.service.AuthenticationServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthenticationController {
    private final AuthenticationServiceImpl authenticationService;

    @Operation(summary = "Register new user")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED )
    public ApiResponse<?> register(@RequestBody @Valid RegisterRequest registerRequest) {
        authenticationService.register(registerRequest);
        return ApiResponse.success("Vui lòng kiểm tra email để xác thực tài khoản");
    }

    @Operation(summary = "Resend verification email")
    @PostMapping("/resend-verification-email")
    public ApiResponse<?> resendNewVerificationEmail(@RequestBody @Valid EmailRequest resendVerificationEmailRequest) {
        authenticationService.resendNewVerificationEmail(resendVerificationEmailRequest.getEmail());
        return ApiResponse.success("Vui lòng kiểm tra email để xác thực tài khoản");
    }

    @Operation(summary = "Verify account")
    @PostMapping("/verify")
    public ApiResponse<?> verifyAccount(@RequestBody @Valid VerifyAccountRequest verifyAccountRequest) {
        authenticationService.verifyAccount(verifyAccountRequest.getToken(), verifyAccountRequest.getSignature());
        return ApiResponse.success("Xác thực tài khoản thành công");
    }

    @Operation(summary = "Login")
    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> login(@RequestBody @Valid AuthenticationRequest loginRequest) {
        return ApiResponse.success(authenticationService.login(loginRequest));
    }

    @Operation(summary = "Logout", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    public ApiResponse<?> logout(@RequestBody @Valid RefreshTokenRequest refreshToken) {
        authenticationService.logout(refreshToken);
        return ApiResponse.success("Đăng xuất thành công");
    }

    @Operation(summary = "Refresh token")
    @PostMapping("/refresh")
    public ApiResponse<AuthenticationResponse> refreshToken(@RequestBody @Valid RefreshTokenRequest refreshToken) {
        return ApiResponse.success(authenticationService.refreshToken(refreshToken));
    }

    @Operation(summary = "Forgot password")
    @PostMapping("/forgot-password")
    public ApiResponse<?> forgotPassword(@RequestBody @Valid EmailRequest emailRequest) {
        authenticationService.forgotPassword(emailRequest.getEmail());
        return ApiResponse.success("Vui lòng kiểm tra email để đặt lại mật khẩu");
    }

    @Operation(summary = "Check reset password token")
    @PostMapping("/check-reset-password-token")
    public ApiResponse<?> checkResetPasswordToken(@RequestBody ResetPasswordRequest resetPasswordRequest) {
        if (StringUtils.isBlank(resetPasswordRequest.getToken()) || StringUtils.isBlank(resetPasswordRequest.getSignature())) {
            return ApiResponse.success(false);
        }
        return ApiResponse.success(authenticationService.checkResetPasswordToken(resetPasswordRequest));
    }

    @Operation(summary = "Reset password")
    @PostMapping("/reset-password")
    public ApiResponse<?> resetPassword(@RequestBody @Valid ResetPasswordRequest resetPasswordRequest) {
        authenticationService.resetPassword(resetPasswordRequest);
        return ApiResponse.success("Đặt lại mật khẩu thành công");
    }
    
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Change password")
    @PutMapping("/change-password")
    public ApiResponse<Boolean> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
    	Boolean ok = authenticationService.changePassword(request);
        return ApiResponse.success("Đổi mật khẩu thành công", ok);
    }

}
