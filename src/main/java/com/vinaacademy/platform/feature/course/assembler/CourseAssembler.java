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
import jakarta.validation.constraints.NotNull;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;

/**
 * Assembler for course-related DTOs. Centralizes the logic for building complex course DTOs with
 * related entities.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CourseAssembler {
  private final CourseInstructorRepository courseInstructorRepository;

  /**
   * Build a complete CourseDetailsResponse for the given Course.
   *
   * <p>Maps the Course to a CourseDetailsResponse, populates instructors (and owner) using either
   * the initialized association or repository-backed projections, processes ordered sections and
   * their ordered lessons, and includes any course reviews.
   *
   * @param course the non-null Course entity to assemble details from
   * @return a fully populated CourseDetailsResponse
   */
  public CourseDetailsResponse assembleCourseDetailsResponse(@NotNull Course course) {
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
      List<CourseReviewDto> reviewDtos =
          course.getCourseReviews().stream().map(CourseReviewMapper.INSTANCE::toDto).toList();
      response.setReviews(reviewDtos);
    }

    return response;
  }

  /**
   * Populates the given CourseDetailsResponse with the course's instructors and the owner instructor.
   *
   * <p>If the course's instructors association is already initialized, this method extracts User
   * entities from CourseInstructor entries; otherwise it queries CourseInstructorRepository for
   * InstructorInfo projections. In both flows it performs null-safe filtering, identifies the owner
   * using {@code Boolean.TRUE.equals(...)} on the "isOwner" flag, maps users to DTOs via
   * {@code UserMapper}, sets the instructors list on the response, and sets the owner instructor if
   * present.
   *
   * @param course the Course to read instructors from
   * @param response the CourseDetailsResponse to populate with instructor DTOs
   */
  private void buildInstructorsResponse(Course course, CourseDetailsResponse response) {
    boolean isFetchInstructors = Hibernate.isInitialized(course.getInstructors());

    List<User> instructorUsers;
    Optional<User> ownerUser;

    if (isFetchInstructors) {
      instructorUsers =
          course.getInstructors().stream()
              .filter(Objects::nonNull)
              .map(CourseInstructor::getInstructor)
              .filter(Objects::nonNull)
              .toList();

      ownerUser =
          course.getInstructors().stream()
              .filter(Objects::nonNull)
              .filter(ci -> Boolean.TRUE.equals(ci.getIsOwner()))
              .map(CourseInstructor::getInstructor)
              .filter(Objects::nonNull)
              .findFirst();

    } else {
      List<InstructorInfo> projections = courseInstructorRepository.findByCourseId(course.getId());

      instructorUsers =
          projections.stream()
              .filter(Objects::nonNull)
              .map(InstructorInfo::getInstructor)
              .filter(Objects::nonNull)
              .toList();

      ownerUser =
          projections.stream()
              .filter(Objects::nonNull)
              .filter(ci -> Boolean.TRUE.equals(ci.getIsOwner()))
              .map(InstructorInfo::getInstructor)
              .filter(Objects::nonNull)
              .findFirst();
    }

    response.setInstructors(instructorUsers.stream().map(UserMapper.INSTANCE::toDto).toList());
    ownerUser.map(UserMapper.INSTANCE::toDto).ifPresent(response::setOwnerInstructor);
  }

  /**
   * Convert a list of Section entities into SectionDto objects with their lessons mapped and ordered.
   *
   * <p>Returns an empty list if {@code sections} is null or empty. Sections and lessons are sorted
   * by their {@code orderIndex} with null indices placed last. Each Section's lessons are mapped to
   * LessonDto via {@code LessonMapper}.
   *
   * @param sections the sections to process (may be null)
   * @return an ordered list of SectionDto objects with ordered LessonDto lists
   */
  public List<SectionDto> processSectionsAndLessons(List<Section> sections) {
    if (sections == null || sections.isEmpty()) {
      return List.of();
    }

    return sections.stream()
        .sorted(
            Comparator.comparing(
                Section::getOrderIndex, Comparator.nullsLast(Comparator.naturalOrder())))
        .map(
            section -> {
              SectionDto sectionDto = SectionMapper.INSTANCE.toDto(section);
              // Fetch and map lessons for each section
              sectionDto.setLessons(
                  section.getLessons().stream()
                      .sorted(
                          Comparator.comparing(
                              Lesson::getOrderIndex,
                              Comparator.nullsLast(Comparator.naturalOrder())))
                      .map(LessonMapper.INSTANCE::lessonToLessonDto)
                      .toList());
              return sectionDto;
            })
        .toList();
  }

  /**
   * Builds ordered SectionDto objects whose LessonDto children are annotated with per-user progress.
   *
   * Sections and lessons are sorted by their `orderIndex` (ascending). If `sections` is null or empty,
   * an empty list is returned. For each lesson, the method looks up progress in `progressMap` by the
   * lesson's UUID; if no entry is present, an empty `UserProgress` is attached.
   *
   * @param sections list of Section entities to convert (may be null)
   * @param progressMap mapping from lesson UUID to the user's progress for that lesson; missing keys
   *     result in a default, empty UserProgress being set on the LessonDto
   * @return an ordered list of SectionDto with lessons populated and each LessonDto carrying its
   *     currentUserProgress
   */
  public List<SectionDto> processSectionsAndLessonsWithProgress(
      List<Section> sections, Map<UUID, UserProgress> progressMap) {
    if (sections == null || sections.isEmpty()) {
      return List.of();
    }

    return sections.stream()
        .sorted(java.util.Comparator.comparing(Section::getOrderIndex))
        .map(
            section -> {
              SectionDto sectionDto = SectionMapper.INSTANCE.toDto(section);

              // Map lessons with progress
              List<LessonDto> lessonDtos =
                  section.getLessons().stream()
                      .sorted(java.util.Comparator.comparing(Lesson::getOrderIndex))
                      .map(
                          lesson -> {
                            LessonDto lessonDto = LessonMapper.INSTANCE.lessonToLessonDto(lesson);
                            // Add user progress if available
                            UserProgress userProgress =
                                progressMap.getOrDefault(lesson.getId(), new UserProgress());
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
