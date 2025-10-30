package com.vinaacademy.platform.feature.user.service;

import com.vinaacademy.platform.exception.BadRequestException;
import com.vinaacademy.platform.feature.user.UserRepository;
import com.vinaacademy.platform.feature.user.entity.User;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {
    private final UserRepository userRepository;

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_TIME_DURATION = 15; /**
     * Loads a UserDetails by username (email), unlocking the account if a prior lock has expired.
     *
     * Retrieves the user identified by the given username (email) and returns it as a UserDetails instance.
     * If the user is currently disabled due to a lock and the lock time has expired, the account is unlocked
     * (enabled, failed attempts reset, lock time cleared) before returning.
     *
     * @param username the user's email used as the username
     * @return the found User as a UserDetails instance
     * @throws BadRequestException if no user with the given email is found
     */

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmailWithRoles(username).orElseThrow(() -> BadRequestException.message("User is invalid."));
        if (!user.isEnabled() && isLockTimeExpired(user)) {
            unlockAccount(user);
        }

        return user;
    }

    /**
     * Increments the stored failed-login counter for the user identified by the given email.
     *
     * If the user is not found this method returns silently. When the counter reaches or
     * exceeds the configured maximum failed attempts the account is locked (disabled and
     * lock time set); otherwise the updated counter is persisted.
     *
     * @param username the user's email (used as username) whose failed-attempt counter will be incremented
     */
    @Transactional
    public void increaseFailedAttempts(String username) {
        User user = userRepository.findByEmailWithRoles(username).orElse(null);
        if (user == null) {
            return;
        }
        int newFailedAttempts = user.getFailedAttempts() + 1;
        user.setFailedAttempts(newFailedAttempts);

        if (newFailedAttempts >= MAX_FAILED_ATTEMPTS) {
            lockAccount(user);
        } else {
            userRepository.save(user);
        }
    }

    @Transactional
    public void resetFailedAttempts(String username) {
        User user = userRepository.findByEmailWithRoles(username).orElse(null);
        if (user == null) {
            return;
        }
        user.setFailedAttempts(0);
        userRepository.save(user);
    }

    private void lockAccount(User user) {
        user.setEnabled(false);
        user.setLockTime(LocalDateTime.now().plusMinutes(LOCK_TIME_DURATION));
        userRepository.save(user);
    }

    private boolean isLockTimeExpired(User user) {
        LocalDateTime lockTime = user.getLockTime();
        return lockTime != null && lockTime.isBefore(LocalDateTime.now());
    }

    /**
     * Re-enables a locked user account and clears its lock state.
     *
     * This resets the user's failed-attempt counter to 0, clears the lock timestamp,
     * marks the account enabled, and persists the updated user.
     *
     * @param user the User entity to unlock and persist
     */
    private void unlockAccount(User user) {
        user.setEnabled(true);
        user.setFailedAttempts(0);
        user.setLockTime(null);
        userRepository.save(user);
    }
}
