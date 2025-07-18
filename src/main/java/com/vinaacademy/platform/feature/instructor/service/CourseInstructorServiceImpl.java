package com.vinaacademy.platform.feature.instructor.service;

import com.vinaacademy.platform.exception.BadRequestException;
import com.vinaacademy.platform.feature.course.entity.Course;
import com.vinaacademy.platform.feature.course.repository.CourseRepository;
import com.vinaacademy.platform.feature.instructor.CourseInstructor;
import com.vinaacademy.platform.feature.instructor.dto.CourseInstructorDto;
import com.vinaacademy.platform.feature.instructor.dto.CourseInstructorDtoRequest;
import com.vinaacademy.platform.feature.instructor.mapper.CourseInstructorMapper;
import com.vinaacademy.platform.feature.instructor.repository.CourseInstructorRepository;
import com.vinaacademy.platform.feature.user.UserRepository;
import com.vinaacademy.platform.feature.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CourseInstructorServiceImpl implements CourseInstructorService {

    private final CourseInstructorRepository instructorRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    @Override
    public CourseInstructorDto createCourseInstructor(CourseInstructorDtoRequest request) {
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> BadRequestException.message("Không tìm thấy user"));
        Course course = courseRepository.findById(request.getCourseId())
            .orElseThrow(() -> BadRequestException.message("Không tìm thấy khóa học đó"));

        CourseInstructor instructor = CourseInstructor.builder()
        		.course(course)
        		.instructor(user)
        		.isOwner(request.getIsOwner())
        		.build();
       
        instructorRepository.save(instructor);
        return CourseInstructorMapper.INSTANCE.toDto(instructor);
    }
}
