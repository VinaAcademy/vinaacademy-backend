package com.vinaacademy.platform.feature.user.service;

import com.vinaacademy.platform.feature.user.UserMapper;
import com.vinaacademy.platform.feature.user.UserRepository;
import com.vinaacademy.platform.feature.user.dto.*;
import com.vinaacademy.platform.feature.user.entity.User;
import com.vinaacademy.platform.feature.user.role.entity.Role;
import com.vinaacademy.platform.feature.user.role.repository.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of AdminUserService for managing users in admin panel
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<AdminUserDto> getAllUsers(UserFilterRequest filterRequest, Pageable pageable) {
        log.info("Fetching users with filters: {}", filterRequest);
        
        Specification<User> spec = buildSpecification(filterRequest);
        Page<User> users = userRepository.findAll(spec, pageable);
        
        return users.map(this::mapToAdminUserDto);
    }

    @Override
    public UserStatisticsDto getUserStatistics() {
        log.info("Calculating user statistics");
        
        long totalUsers = userRepository.count();
        
        // Count by roles
        long totalStudents = userRepository.countByRolesCode("student");
        long totalInstructors = userRepository.countByRolesCode("instructor");
        long totalAdmins = userRepository.countByRolesCode("admin");
        long totalStaff = userRepository.countByRolesCode("staff");
        
        // Count by status
        long activeUsers = userRepository.countByEnabled(true);
        long inactiveUsers = userRepository.countByEnabled(false);
        long lockedUsers = userRepository.countByLockTimeIsNotNull();
        
        // Count new users
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfWeek = now.minusWeeks(1);
        LocalDateTime startOfMonth = now.minusMonths(1);
        
        long newUsersToday = userRepository.countByCreatedDateAfter(startOfToday);
        long newUsersThisWeek = userRepository.countByCreatedDateAfter(startOfWeek);
        long newUsersThisMonth = userRepository.countByCreatedDateAfter(startOfMonth);
        
        long collaborators = userRepository.countByIsCollaborator(true);
        
        return UserStatisticsDto.builder()
                .totalUsers(totalUsers)
                .totalStudents(totalStudents)
                .totalInstructors(totalInstructors)
                .totalAdmins(totalAdmins)
                .totalStaff(totalStaff)
                .activeUsers(activeUsers)
                .inactiveUsers(inactiveUsers)
                .lockedUsers(lockedUsers)
                .newUsersToday(newUsersToday)
                .newUsersThisWeek(newUsersThisWeek)
                .newUsersThisMonth(newUsersThisMonth)
                .collaborators(collaborators)
                .build();
    }

    @Override
    public AdminUserDto getUserById(UUID userId) {
        log.info("Fetching user by ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
        
        return mapToAdminUserDto(user);
    }

    @Override
    @Transactional
    public AdminUserDto createUser(CreateUserRequest request) {
        log.info("Creating new user with username: {}", request.getUsername());
        
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }
        
        // Check if phone already exists (if provided)
        if (request.getPhone() != null && !request.getPhone().isEmpty() 
                && userRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("Phone number already exists: " + request.getPhone());
        }
        
        // Fetch roles by codes
        Set<Role> roles = new HashSet<>();
        for (String roleCode : request.getRoleCodes()) {
            Role role = roleRepository.findByCode(roleCode)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid role code: " + roleCode));
            roles.add(role);
        }
        
        // Build new user
        User newUser = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .avatarUrl(request.getAvatarUrl())
                .description(request.getDescription())
                .birthday(request.getBirthday())
                .roles(roles)
                .isCollaborator(false)
                .enabled(request.getIsEnabled() != null ? request.getIsEnabled() : true)
                .failedAttempts(0)
                .build();
        
        User savedUser = userRepository.save(newUser);
        log.info("User created successfully with ID: {}", savedUser.getId());
        
        return mapToAdminUserDto(savedUser);
    }

    @Override
    @Transactional
    public AdminUserDto updateUserStatus(UUID userId, UpdateUserStatusRequest request) {
        log.info("Updating user status for ID: {}, enabled: {}", userId, request.getEnabled());
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
        
        user.setEnabled(request.getEnabled());
        
        // If disabling user, clear lock time
        if (!request.getEnabled()) {
            user.setLockTime(null);
            user.setFailedAttempts(0);
        }
        
        User savedUser = userRepository.save(user);
        log.info("User status updated successfully. Reason: {}", request.getReason());
        
        return mapToAdminUserDto(savedUser);
    }

    @Override
    @Transactional
    public AdminUserDto updateUserRoles(UUID userId, UpdateUserRoleRequest request) {
        log.info("Updating user roles for ID: {}, new roles: {}", userId, request.getRoleCodes());
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
        
        // Fetch roles by codes
        Set<Role> newRoles = new HashSet<>();
        for (String roleCode : request.getRoleCodes()) {
            Role role = roleRepository.findByCode(roleCode)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid role code: " + roleCode));
            newRoles.add(role);
        }
        
        user.setRoles(newRoles);
        User savedUser = userRepository.save(user);
        
        log.info("User roles updated successfully. Reason: {}", request.getReason());
        
        return mapToAdminUserDto(savedUser);
    }

    @Override
    @Transactional
    public void deleteUser(UUID userId) {
        log.info("Soft deleting user with ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
        
        // Soft delete by disabling the user
        user.setEnabled(false);
        userRepository.save(user);
        
        log.info("User soft deleted successfully");
    }

    @Override
    @Transactional
    public AdminUserDto updateCollaboratorStatus(UUID userId, boolean isCollaborator) {
        log.info("Updating collaborator status for user ID: {}, isCollaborator: {}", userId, isCollaborator);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
        
        user.setCollaborator(isCollaborator);
        User savedUser = userRepository.save(user);
        
        return mapToAdminUserDto(savedUser);
    }

    /**
     * Build JPA Specification based on filter request
     */
    private Specification<User> buildSpecification(UserFilterRequest filterRequest) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Keyword search (name, email, username)
            if (filterRequest.getKeyword() != null && !filterRequest.getKeyword().trim().isEmpty()) {
                String keyword = "%" + filterRequest.getKeyword().toLowerCase() + "%";
                Predicate namePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("fullName")), keyword);
                Predicate emailPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")), keyword);
                Predicate usernamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("username")), keyword);
                
                predicates.add(criteriaBuilder.or(namePredicate, emailPredicate, usernamePredicate));
            }

            // Role filter
            if (filterRequest.getRole() != null && !filterRequest.getRole().isEmpty()) {
                predicates.add(criteriaBuilder.equal(
                        root.join("roles").get("code"), filterRequest.getRole()));
            }

            // Status filter (ACTIVE, INACTIVE, LOCKED)
            if (filterRequest.getStatus() != null && !filterRequest.getStatus().isEmpty()) {
                switch (filterRequest.getStatus().toUpperCase()) {
                    case "ACTIVE":
                        predicates.add(criteriaBuilder.isTrue(root.get("enabled")));
                        predicates.add(criteriaBuilder.isNull(root.get("lockTime")));
                        break;
                    case "INACTIVE":
                        predicates.add(criteriaBuilder.isFalse(root.get("enabled")));
                        break;
                    case "LOCKED":
                        predicates.add(criteriaBuilder.isNotNull(root.get("lockTime")));
                        break;
                }
            }

            // Date range filter
            if (filterRequest.getFromDate() != null) {
                LocalDateTime fromDateTime = filterRequest.getFromDate().atStartOfDay();
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("createdDate"), fromDateTime));
            }
            
            if (filterRequest.getToDate() != null) {
                LocalDateTime toDateTime = filterRequest.getToDate().atTime(LocalTime.MAX);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("createdDate"), toDateTime));
            }

            // Collaborator filter
            if (filterRequest.getIsCollaborator() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("isCollaborator"), filterRequest.getIsCollaborator()));
            }

            // Enabled filter
            if (filterRequest.getIsEnabled() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("enabled"), filterRequest.getIsEnabled()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Map User entity to AdminUserDto
     */
    private AdminUserDto mapToAdminUserDto(User user) {
        // Extract role codes and determine primary role
        List<String> roleCodes = user.getRoles().stream()
                .map(Role::getCode)
                .collect(Collectors.toList());
        
        String primaryRole = determinePrimaryRole(roleCodes);
        
        // Count enrollments and courses
        // Use separate queries to avoid N+1 problem
        long enrollmentCount = userRepository.countEnrollmentsByUserId(user.getId());
        long createdCourseCount = userRepository.countCreatedCoursesByUserId(user.getId());
        long completedCourseCount = userRepository.countCompletedCoursesByUserId(user.getId());
        
        return AdminUserDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .username(user.getUsername())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .description(user.getDescription())
                .isCollaborator(user.isCollaborator())
                .birthday(user.getBirthday())
                .roles(roleCodes)
                .primaryRole(primaryRole)
                .isActive(user.isEnabled())
                .isEnabled(user.isEnabled())
                .lockTime(user.getLockTime())
                .failedAttempts(user.getFailedAttempts())
                .enrollmentCount(enrollmentCount)
                .createdCourseCount(createdCourseCount)
                .completedCourseCount(completedCourseCount)
                .lastActive(user.getUpdatedDate())
                .joinDate(user.getCreatedDate())
                .createdDate(user.getCreatedDate())
                .updatedDate(user.getUpdatedDate())
                .build();
    }

    /**
     * Determine primary role based on role hierarchy
     */
    private String determinePrimaryRole(List<String> roleCodes) {
        if (roleCodes.contains("admin")) return "ADMIN";
        if (roleCodes.contains("staff")) return "STAFF";
        if (roleCodes.contains("instructor")) return "INSTRUCTOR";
        if (roleCodes.contains("student")) return "STUDENT";
        return "USER";
    }
}
