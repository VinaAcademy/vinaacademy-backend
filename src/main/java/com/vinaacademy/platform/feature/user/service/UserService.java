package com.vinaacademy.platform.feature.user.service;

import java.util.UUID;

import com.vinaacademy.platform.feature.user.dto.UpdateUserInfoRequest;
import com.vinaacademy.platform.feature.user.dto.UserDto;
import com.vinaacademy.platform.feature.user.dto.UserViewDto;

public interface UserService {
    void createTestingData();

    UserDto getCurrentUser();
    
    UserDto updateUserInfo(UpdateUserInfoRequest request);
    
    UserViewDto viewUser(UUID userId);
}
