package com.vinaacademy.platform.feature.instructor.projection;

import com.vinaacademy.platform.feature.user.entity.User;

public interface InstructorInfo {
    User getInstructor();

    Boolean getIsOwner();
}
