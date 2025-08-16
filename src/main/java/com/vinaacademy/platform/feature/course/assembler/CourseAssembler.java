package com.vinaacademy.platform.feature.course.assembler;

import com.vinaacademy.platform.feature.course.dto.CourseDetailsResponse;
import com.vinaacademy.platform.feature.course.entity.Course;
import com.vinaacademy.platform.feature.course.mapper.CourseMapper;
import com.vinaacademy.platform.feature.instructor.CourseInstructor;
import com.vinaacademy.platform.feature.instructor.projection.InstructorInfo;
import com.vinaacademy.platform.feature.instructor.repository.CourseInstructorRepository;
import com.vinaacademy.platform.feature.lesson.dto.LessonDto;
import com.vinaacademy.platform.feature.lesson.entity.Lesson;
import com.vinaacademy.platform.feature.lesson.entity.UserProgress;
import com.vinaacademy.platform.feature.lesson.mapper.LessonMapper;
import com.vinaacademy.platform.feature.review.dto.CourseReviewDto;
import com.vinaacademy.platform.feature.review.mapper.CourseReviewMapper;
import com.vinaacademy.platform.feature.section.dto.SectionDto;
import com.vinaacademy.platform.feature.section.entity.Section;
import com.vinaacademy.platform.feature.section.mapper.SectionMapper;
import com.vinaacademy.platform.feature.user.UserMapper;
import com.vinaacademy.platform.feature.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Assembler for course-related DTOs.
 * Centralizes the logic for building complex course DTOs with related entities.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CourseAssembler {
    private final CourseInstructorRepository courseInstructorRepository;

    /**
     * Assemble course details response with instructors, sections, lessons, and reviews
     *
     * @param course The course entity
     * @return Complete course details response
     */
    public CourseDetailsResponse assembleCourseDetailsResponse(Course course) {
        log.debug("Assembling course details response for course: {}", course.getId());

        // Use CourseMapper to create the base course details
        CourseDetailsResponse response = CourseMapper.INSTANCE.toCourseDetailsResponse(course);

        // Fetch and set instructors and owner instructor
        buildInstructorsResponse(course, response);

        // Process sections with lessons
        List<SectionDto> sectionDtos = processSectionsAndLessons(course.getSections());
        response.setSections(sectionDtos);

        // Fetch course reviews
        if (course.getCourseReviews() != null && !course.getCourseReviews().isEmpty()) {
            List<CourseReviewDto> reviewDtos = course.getCourseReviews().stream()
                    .map(CourseReviewMapper.INSTANCE::toDto)
                    .toList();
            response.setReviews(reviewDtos);
        }

        return response;
    }

    private void buildInstructorsResponse(Course course, CourseDetailsResponse response) {
        boolean isFetchInstructors = Hibernate.isInitialized(course.getInstructors());
        Stream<User> instructorUsers;
        Optional<User> ownerUser;
        if (isFetchInstructors) {
            var cis = course.getInstructors();
            instructorUsers = cis.stream().map(CourseInstructor::getInstructor);
            ownerUser = cis.stream()
                    .filter(CourseInstructor::getIsOwner)
                    .map(CourseInstructor::getInstructor)
                    .findFirst();
        } else {
            var projections = courseInstructorRepository.findByCourseId(course.getId());
            instructorUsers = projections.stream().map(InstructorInfo::getInstructor);
            ownerUser = projections.stream()
                    .filter(InstructorInfo::getIsOwner)
                    .map(InstructorInfo::getInstructor)
                    .findFirst();
        }
        response.setInstructors(
                instructorUsers.map(UserMapper.INSTANCE::toDto).toList()
        );
        ownerUser
                .map(UserMapper.INSTANCE::toDto)
                .ifPresent(response::setOwnerInstructor);
    }

    /**
     * Process sections and their lessons, creating DTOs with sorted order
     *
     * @param sections List of section entities
     * @return List of section DTOs with ordered lessons
     */
    public List<SectionDto> processSectionsAndLessons(List<Section> sections) {
        if (sections == null || sections.isEmpty()) {
            return List.of();
        }

        return sections.stream()
                .sorted(java.util.Comparator.comparing(Section::getOrderIndex))
                .map(section -> {
                    SectionDto sectionDto = SectionMapper.INSTANCE.toDto(section);
                    // Fetch and map lessons for each section
                    sectionDto.setLessons(section.getLessons().stream()
                            .sorted(java.util.Comparator.comparing(Lesson::getOrderIndex))
                            .map(LessonMapper.INSTANCE::lessonToLessonDto)
                            .toList());
                    return sectionDto;
                })
                .toList();
    }

    /**
     * Process sections and lessons with user progress information
     *
     * @param sections    List of section entities
     * @param progressMap Map of lesson ID to user progress
     * @return List of section DTOs with lessons including progress
     */
    public List<SectionDto> processSectionsAndLessonsWithProgress(List<Section> sections, Map<UUID, UserProgress> progressMap) {
        if (sections == null || sections.isEmpty()) {
            return List.of();
        }

        return sections.stream()
                .sorted(java.util.Comparator.comparing(Section::getOrderIndex))
                .map(section -> {
                    SectionDto sectionDto = SectionMapper.INSTANCE.toDto(section);

                    // Map lessons with progress
                    List<LessonDto> lessonDtos = section.getLessons().stream()
                            .sorted(java.util.Comparator.comparing(Lesson::getOrderIndex))
                            .map(lesson -> {
                                LessonDto lessonDto = LessonMapper.INSTANCE.lessonToLessonDto(lesson);
                                // Add user progress if available
                                UserProgress userProgress = progressMap.getOrDefault(
                                        lesson.getId(),
                                        new UserProgress());
                                lessonDto.setCurrentUserProgress(userProgress);
                                return lessonDto;
                            })
                            .toList();

                    sectionDto.setLessons(lessonDtos);
                    return sectionDto;
                })
                .toList();
    }
}
