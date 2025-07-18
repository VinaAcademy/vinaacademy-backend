package com.vinaacademy.platform.feature.user.auth.helpers;

import com.vinaacademy.platform.feature.user.constant.AuthConstants;
import com.vinaacademy.platform.feature.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AccessHelper {

    public boolean isAdmin(User user) {
        return checkRole(user, AuthConstants.ADMIN_ROLE);
    }

    public boolean isStaff(User user) {
        return checkRole(user, AuthConstants.STAFF_ROLE);
    }

    public boolean checkRole(User user, String roleCode) {
        return user.getRoles().stream()
                .anyMatch(role -> roleCode.equals(role.getCode()));
    }
}
