package com.vinaacademy.platform.feature.user.auth.service;

import com.vinaacademy.platform.feature.user.auth.dto.*;

public interface AuthenticationService {
    void register(RegisterRequest registerRequest);

    AuthenticationResponse login(AuthenticationRequest loginRequest);

    void resendNewVerificationEmail(String email);

    void verifyAccount(String token, String signature);

    void logout(RefreshTokenRequest refreshToken);

    AuthenticationResponse refreshToken(RefreshTokenRequest refreshToken);


    void forgotPassword(String email);

    boolean checkResetPasswordToken(ResetPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
    
    boolean changePassword(ChangePasswordRequest request);
}
