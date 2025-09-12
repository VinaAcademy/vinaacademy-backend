package com.vinaacademy.platform.feature.user.auth.service;

import com.vinaacademy.platform.exception.BadRequestException;
import com.vinaacademy.platform.exception.RetryableException;
import com.vinaacademy.platform.feature.common.constant.RetryConstants;
import com.vinaacademy.platform.feature.common.utils.RandomUtils;
import com.vinaacademy.platform.feature.email.config.UrlBuilder;
import com.vinaacademy.platform.feature.email.service.EmailService;
import com.vinaacademy.platform.feature.log.constant.LogConstants;
import com.vinaacademy.platform.feature.log.service.LogService;
import com.vinaacademy.platform.feature.user.UserMapper;
import com.vinaacademy.platform.feature.user.UserRepository;
import com.vinaacademy.platform.feature.user.auth.dto.*;
import com.vinaacademy.platform.feature.user.auth.entity.ActionToken;
import com.vinaacademy.platform.feature.user.auth.entity.RefreshToken;
import com.vinaacademy.platform.feature.user.auth.enums.ActionTokenType;
import com.vinaacademy.platform.feature.user.auth.helpers.SecurityHelper;
import com.vinaacademy.platform.feature.user.auth.repository.ActionTokenRepository;
import com.vinaacademy.platform.feature.user.auth.repository.RefreshTokenRepository;
import com.vinaacademy.platform.feature.user.auth.utils.JwtUtils;
import com.vinaacademy.platform.feature.user.constant.AuthConstants;
import com.vinaacademy.platform.feature.user.entity.User;
import com.vinaacademy.platform.feature.user.role.repository.RoleRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Strings;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActionTokenRepository actionTokenRepository;

    private final UrlBuilder urlBuilder;
    private final LogService logService;
    private final HttpServletRequest httpServletRequest;
    private final RoleRepository roleRepository;
    private final SecurityHelper securityHelper;


    /**
     * Registers a new user in the system.
     *
     * @param registerRequest The registration request containing user details including email, password, retyped password and full name
     * @throws BadRequestException if passwords don't match or if email already exists in the system.
     *                             The user account is initially disabled and requires email verification to be activated.
     */
    @Transactional
    public void register(RegisterRequest registerRequest) {
        if (!Strings.CI.equals(registerRequest.getPassword(), registerRequest.getRetypedPassword())) {
            throw BadRequestException.message("Mật khẩu không khớp");
        }
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw BadRequestException.message("Email đã tồn tại");
        }
        User user = UserMapper.INSTANCE.toUser(registerRequest);
        String username = generateUsername(registerRequest.getFullName());
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEnabled(false);
        user.setRoles(Set.of(roleRepository.findByCode(AuthConstants.STUDENT_ROLE)));
//        user.setRoles(List.of(roleRepository.findByCode(AuthConstants.STUDENT_ROLE)));
        user = userRepository.save(user);

        String token = RandomUtils.generateUUID();

        ActionToken actionToken = ActionToken.builder()
                .user(user)
                .type(ActionTokenType.VERIFY_ACCOUNT)
                .token(token)
                .expiredAt(LocalDateTime.now().plusHours(AuthConstants.ACTION_TOKEN_EXPIRED_HOURS))
                .build();
        actionTokenRepository.save(actionToken);

        logService.log(LogConstants.AUTH_KEY, LogConstants.REGISTER_ACTION, null, user);
        emailService.sendVerificationEmail(user.getEmail(), actionToken.getToken());
    }

    @Retryable(retryFor = {RetryableException.class}, maxAttempts = RetryConstants.DEFAULT_MAX_ATTEMPTS)
    private String generateUsername(String fullName) {
        String username = fullName.toLowerCase().replaceAll("\\s+", "");
        username = username.substring(0, Math.min(username.length(), 10))
                + RandomUtils.generateRandomString(5);
        if (userRepository.existsByUsername(username)) {
            throw RetryableException.message("Tên đăng nhập đã tồn tại");
        }
        return username;
    }

    /**
     * Authenticates a user and generates access and refresh tokens.
     *
     * @param loginRequest The login request containing the user's email and password.
     * @return An AuthenticationResponse containing access and refresh tokens.
     * @throws BadRequestException if the user is not found, not enabled, or locked.
     */
    public AuthenticationResponse login(AuthenticationRequest loginRequest) {
      authenticateUser(loginRequest.getEmail(), loginRequest.getPassword());

      UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());
        if (userDetails == null) {
            throw BadRequestException.message("Không tìm thấy người dùng: " + loginRequest.getEmail());
        }

        if (!userDetails.isEnabled()) {
            throw BadRequestException.message("Tài khoản chưa được xác thực");
        }

        if (!userDetails.isAccountNonLocked()) {
            throw BadRequestException.message("Tài khoản đã bị khóa");
        }


        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);


        RefreshToken token = RefreshToken.builder()
                .token(refreshToken)
                .username(userDetails.getUsername())
                .expireTime(jwtService.getExpirationTime(refreshToken))
                .build();
        refreshTokenRepository.save(token);

        logService.log(LogConstants.AUTH_KEY, LogConstants.LOGIN_ACTION, null, null);
        return new AuthenticationResponse(accessToken, refreshToken);
    }

    private void authenticateUser(String email, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (AuthenticationException ex) {
            String message = authenticationExceptionMessage(ex);
            if (ex instanceof DisabledException) {
                handleDisabledException(email);
            }
            throw BadRequestException.message(message);
        }
    }

    private void handleDisabledException(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> BadRequestException.message("Không tìm thấy người dùng"));

        ActionToken token = actionTokenRepository.findForUpdate(user, ActionTokenType.VERIFY_ACCOUNT)
                .orElseThrow(() -> BadRequestException.message("Không tìm thấy token"));

        LocalDateTime now = LocalDateTime.now();
        if (token.getExpiredAt().isBefore(now)) {
            token.setExpiredAt(now.plusHours(AuthConstants.ACTION_TOKEN_EXPIRED_HOURS));
            actionTokenRepository.save(token);
            emailService.sendVerificationEmail(email, token.getToken());
        }
    }


    private static String authenticationExceptionMessage(AuthenticationException ex) {
        String message = "Lỗi xác thực không xác định";
        if (ex instanceof BadCredentialsException) {
            message = "Sai tên đăng nhập hoặc mật khẩu";
        } else if (ex instanceof DisabledException) {
            message = "Tài khoản chưa được xác thực, vui lòng kiểm tra email";
        } else if (ex instanceof LockedException) {
            message = "Tài khoản đã bị khóa";
        } else if (ex instanceof AccountExpiredException) {
            message = "Tài khoản đã hết hạn";
        }
        return message;
    }

    /**
     * Resends the verification email to the specified user.
     *
     * @param email The email address of the user.
     * @throws BadRequestException if the user or verification token is not found.
     */
    @Override
    @Transactional
    public void resendNewVerificationEmail(String email) {
        User user = userRepository.findByEmailWithRoles(email)
                .orElseThrow(() -> BadRequestException.message("Không tìm thấy người dùng: " + email));

        ActionToken actionToken = actionTokenRepository.findByUserAndType(user, ActionTokenType.VERIFY_ACCOUNT)
                .orElseThrow(() -> BadRequestException.message("Không tìm thấy token xác thực"));

        actionToken.setExpiredAt(LocalDateTime.now().plusHours(AuthConstants.ACTION_TOKEN_EXPIRED_HOURS));

        actionTokenRepository.save(actionToken);

        logService.log(LogConstants.AUTH_KEY, LogConstants.RESEND_VERIFY_EMAIL_ACTION, null, actionToken);

        emailService.sendVerificationEmail(user.getEmail(), actionToken.getToken());
    }

    /**
     * Logs out a user by invalidating the provided refresh token.
     *
     * @param refreshToken The refresh token request to invalidate.
     * @throws BadRequestException if the token is invalid.
     */
    public void logout(RefreshTokenRequest refreshToken) {
        String username = jwtService.extractUsername(JwtUtils.getJwtToken(httpServletRequest));

        RefreshToken token = refreshTokenRepository.findByTokenAndUsername(refreshToken.getRefreshToken(),
                        username)
                .orElseThrow(() -> BadRequestException.message("Token không hợp lệ"));

        refreshTokenRepository.delete(token);

        logService.log(LogConstants.AUTH_KEY, LogConstants.LOGOUT_ACTION, null, null);
    }

    /**
     * Refreshes the access token using the provided refresh token.
     *
     * @param refreshToken The refresh token request.
     * @return An AuthenticationResponse containing a new access token and the provided refresh token.
     * @throws BadRequestException if the token is invalid or expired.
     */
    public AuthenticationResponse refreshToken(RefreshTokenRequest refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken.getRefreshToken())
                .orElseThrow(() -> BadRequestException.message("Token không hợp lệ"));

        if (token.getExpireTime().isBefore(LocalDateTime.now())) {
            throw BadRequestException.message("Token đã hết hạn");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(token.getUsername());

        String accessToken = jwtService.generateAccessToken(userDetails);
        return new AuthenticationResponse(accessToken, refreshToken.getRefreshToken());
    }

    /**
     * Verifies a user account using a verification token and signature.
     *
     * @param token     The verification token.
     * @param signature The signature to validate the token.
     * @throws BadRequestException if the signature or token is invalid.
     */
    @Transactional
    @Override
    public void verifyAccount(String token, String signature) {
        if (!urlBuilder.isSignatureValid(token, signature)) {
            throw BadRequestException.message("Chữ ký không hợp lệ");
        }

        ActionToken actionToken = actionTokenRepository.findByTokenAndType(token, ActionTokenType.VERIFY_ACCOUNT)
                .orElseThrow(() -> BadRequestException.message("Token không hợp lệ"));

        User user = actionToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        actionTokenRepository.delete(actionToken);

        emailService.sendWelcomeEmail(user);

        logService.log(LogConstants.AUTH_KEY, LogConstants.VERIFY_ACCOUNT_ACTION, null, Map.of("email", user.getEmail()));
    }

    /**
     * Initiates the password reset process for a given user's email.
     *
     * @param email The email address of the user.
     * @throws BadRequestException if the user is not found.
     */
    @Override
    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> BadRequestException.message("Không tìm thấy người dùng: " + email));

        String token = RandomUtils.generateUUID();

        ActionToken actionToken = ActionToken.builder()
                .user(user)
                .type(ActionTokenType.RESET_PASSWORD)
                .expiredAt(LocalDateTime.now().plusHours(AuthConstants.ACTION_TOKEN_EXPIRED_HOURS))
                .token(token)
                .build();
        actionTokenRepository.save(actionToken);

        emailService.sendPasswordResetEmail(user, actionToken.getToken());

        logService.log(LogConstants.AUTH_KEY, LogConstants.FORGOT_PASSWORD_ACTION, null, Map.of("email", email));
    }

    /**
     * Validates a password reset token.
     *
     * @param request The reset password request containing the token and signature.
     * @return true if the token is valid and not expired; false otherwise.
     * @throws BadRequestException if the token is invalid.
     */
    @Override
    public boolean checkResetPasswordToken(ResetPasswordRequest request) {
        if (!urlBuilder.isSignatureValid(request.getToken(), request.getSignature())) {
            return false;
        }
        ActionToken actionToken = actionTokenRepository.findByTokenAndType(request.getToken(), ActionTokenType.RESET_PASSWORD)
                .orElseThrow(() -> BadRequestException.message("Token không hợp lệ"));

        return actionToken.getExpiredAt().isAfter(LocalDateTime.now());
    }

    /**
     * Resets the user's password using a valid token and signature.
     *
     * @param request The reset password request containing the new password, token, and signature.
     * @throws BadRequestException if the signature is invalid, or the token is invalid or expired.
     */
    @Override
    public void resetPassword(ResetPasswordRequest request) {
        if (!urlBuilder.isSignatureValid(request.getToken(), request.getSignature())) {
            throw BadRequestException.message("Chữ ký không hợp lệ");
        }

        ActionToken actionToken = actionTokenRepository.findByTokenAndType(request.getToken(), ActionTokenType.RESET_PASSWORD)
                .orElseThrow(() -> BadRequestException.message("Token không hợp lệ"));

        if (actionToken.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw BadRequestException.message("Token đã hết hạn");
        }

        if (!Strings.CI.equals(request.getPassword(), request.getRetypedPassword())) {
            throw BadRequestException.message("Mật khẩu không khớp");
        }

        User user = actionToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        actionTokenRepository.delete(actionToken);

        logService.log(LogConstants.AUTH_KEY, LogConstants.RESET_PASSWORD_ACTION, null, Map.of("email", user.getEmail()));
    }

    @Override
    @Transactional
    public boolean changePassword(ChangePasswordRequest request) {
        User user = securityHelper.getCurrentUser();
        if (!Strings.CI.equals(request.getNewPassword(), request.getRetypedPassword())) {
            throw BadRequestException.message("Mật khẩu mới không khớp");
        }

        // Validate current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw BadRequestException.message("Mật khẩu hiện tại không chính xác");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        logService.log(LogConstants.USER_KEY, LogConstants.CHANGE_PASSWORD_ACTION, null,
                Map.of("userId", user.getId().toString()));
        return true;
    }
}
