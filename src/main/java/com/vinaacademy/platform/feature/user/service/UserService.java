package com.vinaacademy.platform.feature.user.service;

import com.vinaacademy.platform.feature.user.dto.UpdateUserInfoRequest;
import com.vinaacademy.platform.feature.user.dto.UserDto;
import com.vinaacademy.platform.feature.user.dto.UserViewDto;
import java.util.UUID;
import org.springframework.data.domain.Page;

public interface UserService {
    void createTestingData();

    UserDto getCurrentUser();
    
    UserDto updateUserInfo(UpdateUserInfoRequest request);
    
    UserViewDto viewUser(UUID userId);

    Page<UserDto> searchUsers(String keyword, int page, int size);
}
