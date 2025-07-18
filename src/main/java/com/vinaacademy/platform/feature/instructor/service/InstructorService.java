package com.vinaacademy.platform.feature.instructor.service;

import com.vinaacademy.platform.feature.instructor.dto.InstructorInfoDto;

import java.util.UUID;

public interface InstructorService {
    InstructorInfoDto getInstructorInfo(UUID instructorId);
    InstructorInfoDto registerAsInstructor();
//    boolean isInstructor(UUID userId);

}
