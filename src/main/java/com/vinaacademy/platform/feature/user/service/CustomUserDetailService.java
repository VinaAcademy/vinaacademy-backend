package com.vinaacademy.platform.feature.user.service;

import com.vinaacademy.platform.exception.BadRequestException;
import com.vinaacademy.platform.feature.user.UserRepository;
import com.vinaacademy.platform.feature.user.entity.User;
import com.vinaacademy.platform.feature.user.role.entity.Permission;
import com.vinaacademy.platform.feature.user.role.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {
    private final UserRepository userRepository;

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_TIME_DURATION = 15; // 15 minutes

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmailWithRoles(username).orElseThrow(() -> BadRequestException.message("User is invalid."));
        if (!user.isEnabled() && isLockTimeExpired(user)) {
            unlockAccount(user);
        }

        String[] roles = user.getRoles().stream().map(Role::getCode).toArray(String[]::new);

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .disabled(!user.isEnabled())
                .accountExpired(false)
                .credentialsExpired(false)
                .accountLocked(user.isLocked()).roles(roles)
                .authorities(getAuthorities(user.getRoles()))
                .build();
    }

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

    private void unlockAccount(User user) {
        user.setEnabled(true);
        user.setFailedAttempts(0);
        user.setLockTime(null);
        userRepository.save(user);
    }


    private Collection<? extends GrantedAuthority> getAuthorities(final Set<Role> roles) {
//        return roles.stream().map(s -> new GrantedAuthority() {
//            @Override
//            public String getAuthority() {
//                return "ROLE_" + s.getName();
//            }
//        }).collect(Collectors.toList());
        return getGrantedAuthorities(getPermissions(roles));
    }

    private Set<String> getPermissions(final Set<Role> role) {
        final Set<String> permissions = new HashSet<>();
        final List<Permission> collection = new ArrayList<>();
        for (final Role item : role) {
            permissions.add("ROLE_" + item.getName());
            collection.addAll(item.getPermissions());
        }
        for (final Permission item : collection) {
            permissions.add(item.getName());
        }
        return permissions;
    }

    private List<GrantedAuthority> getGrantedAuthorities(final Set<String> privileges) {
        return privileges.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
//         List<GrantedAuthority> authorities = new ArrayList<>();
//        for (final String privilege : privileges) {
//            authorities.add(new SimpleGrantedAuthority(privilege));
//        }
//        return authorities;
    }
}
