package com.vinaacademy.platform.feature.migration.course;

import com.vinaacademy.platform.feature.migration.data.StudentData;
import com.vinaacademy.platform.feature.user.UserRepository;
import com.vinaacademy.platform.feature.user.constant.AuthConstants;
import com.vinaacademy.platform.feature.user.entity.User;
import com.vinaacademy.platform.feature.user.role.entity.Role;
import com.vinaacademy.platform.feature.user.role.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentMockService {
    private static final int MAX_RANDOM_STUDENTS = 15;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Transactional
    public List<User> createRandomStudents() {
        Role studentRole = roleRepository.findByCode(AuthConstants.STUDENT_ROLE)
                .orElseThrow(() -> new RuntimeException("Student role not found"));
        List<User> students = StudentData.generateRandomStudents(MAX_RANDOM_STUDENTS, studentRole);
        userRepository.saveAll(students);
        log.info("Generated {} random students", students.size());
        return students;
    }
}
