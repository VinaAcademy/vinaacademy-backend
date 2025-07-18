package com.vinaacademy.platform.feature.user.dto;

import com.vinaacademy.platform.feature.common.dto.BaseDto;
import com.vinaacademy.platform.feature.user.role.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserDto extends BaseDto {
    private UUID id;
    private String fullName;
    private String email;
    private String username;
    private String phone;
    private String avatarUrl;
    private String description;
    private boolean isCollaborator;
    private LocalDate birthday;
    private Set<Role> roles = new HashSet<>();
    private boolean isActive;
}
