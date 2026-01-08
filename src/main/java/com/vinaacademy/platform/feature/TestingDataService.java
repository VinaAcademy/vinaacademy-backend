package com.vinaacademy.platform.feature;

import com.vinaacademy.platform.feature.category.repository.CategoryRepository;
import com.vinaacademy.platform.feature.course.repository.CourseRepository;
import com.vinaacademy.platform.feature.instructor.repository.CourseInstructorRepository;
import com.vinaacademy.platform.feature.migration.CouponMigrationService;
import com.vinaacademy.platform.feature.migration.course.CourseMockService;
import com.vinaacademy.platform.feature.quiz.repository.AnswerRepository;
import com.vinaacademy.platform.feature.quiz.repository.QuestionRepository;
import com.vinaacademy.platform.feature.quiz.repository.QuizRepository;
import com.vinaacademy.platform.feature.reading.repository.ReadingRepository;
import com.vinaacademy.platform.feature.section.repository.SectionRepository;
import com.vinaacademy.platform.feature.user.UserRepository;
import com.vinaacademy.platform.feature.user.constant.AuthConstants;
import com.vinaacademy.platform.feature.user.entity.User;
import com.vinaacademy.platform.feature.user.role.entity.Role;
import com.vinaacademy.platform.feature.user.role.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestingDataService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CategoryRepository categoryRepository;
    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;
    private final CourseInstructorRepository courseInstructorRepository;
    private final ReadingRepository readingRepository;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;

    private final PasswordEncoder passwordEncoder;
    private final CouponMigrationService couponMigrationService;
    private final CourseMockService courseMockService;

    @Transactional
    public void createTestingAuthData() {
        String[] roles = {AuthConstants.ADMIN_ROLE, AuthConstants.STAFF_ROLE,
                AuthConstants.INSTRUCTOR_ROLE, AuthConstants.STUDENT_ROLE};
        if (roleRepository.count() > 0) {
            return;
        }
        for (String role : roles) {
            roleRepository.save(Role.builder()
                    .name(role)
                    .code(role).build());
        }

        Role adminRole = roleRepository.findByCode(AuthConstants.ADMIN_ROLE).orElseThrow();
        Role staffRole = roleRepository.findByCode(AuthConstants.STAFF_ROLE).orElseThrow();
        Role instructorRole = roleRepository.findByCode(AuthConstants.INSTRUCTOR_ROLE).orElseThrow();
        Role studentRole = roleRepository.findByCode(AuthConstants.STUDENT_ROLE).orElseThrow();

        User admin = User.builder()
                .username("admin")
                .password("$2a$12$b0hzE.FH1goAeynP1QRPquPbci5sgxHGBVI9AyuemvKQOutHdyOy2")
                .email("locn562836@gmail.com")
                .enabled(true)
                .roles(Set.of(adminRole, studentRole))
                .build();

        User staff = User.builder()
                .username("staff")
                .password("$2a$12$vqUTYsxFbUbs9XAaKH1.FuUw0nDYWWq1zn5rwAblGSunqbDLsqyWK")
                .email("huuloc2155@gmail.com")
                .enabled(true)
                .roles(Set.of(staffRole, studentRole))
                .build();

        User instructor = User.builder()
                .username("instructor")
                .password("$2a$12$SDQHv9sITBn8DBUxG3oOLeh2JUujZlZuV/W1IUO65C3KnqFxhIJye")
                .email("linhpht263@outlook.com.vn")
                .enabled(true)
                .roles(Set.of(instructorRole, studentRole))
                .fullName("Linh Phan")
                .description("Chuyên gia với hơn 10 năm kinh nghiệm trong lĩnh vực phát triển phần mềm và " +
                        "kiến trúc hệ thống. Linh Phan đã dẫn dắt nhiều dự án chuyển đổi số lớn và " +
                        "đam mê chia sẻ kiến thức về Java, Spring Boot và Microservices đến thế hệ lập trình viên trẻ.")
                .build();

        User student = User.builder()
                .username("student")
                .password("$2a$12$c8xwXNrvHAKNP/Yzirb8MOV/iKnTU3J/aUqC2uCH8E3FmUJ4MUIy.")
                .email("trihung987@gmail.com")
                .enabled(true)
                .roles(Set.of(studentRole))
                .build();

        userRepository.save(admin);
        userRepository.save(staff);
        userRepository.save(instructor);
        userRepository.save(student);
    }

    /**
     * Create seed data for categories and courses from JSON file
     * All courses will have empty sections, zero students, and zero ratings
     */
    @Transactional
    public void createSeedDataFromJson() {
        if (courseRepository.count() > 0) {
            log.info("Courses already exist in the database, skipping seed data creation");
            return;
        }

        couponMigrationService.createCouponSeedData();

        try {
            // Get instructor user or create one if not exists
            User instructor = userRepository.findByUsername("instructor")
                    .orElseGet(() -> {
                        User newInstructor = User.builder()
                                .username("instructor")
                                .password(passwordEncoder.encode("instructor123"))
                                .email("instructor@example.com")
                                .enabled(true)
                                .roles(Set.of(roleRepository.findByCode(AuthConstants.INSTRUCTOR_ROLE).orElseThrow()))
                                .build();
                        return userRepository.save(newInstructor);
                    });

            courseMockService.createCoursesData(instructor);
        } catch (IOException e) {
            log.error("Error reading categories-courses.json", e);
        } catch (Exception e) {
            log.error("Error creating seed data", e);
        }
    }
}
