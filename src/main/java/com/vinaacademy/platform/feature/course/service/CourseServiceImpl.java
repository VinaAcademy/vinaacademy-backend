package com.vinaacademy.platform.feature.course.service;

import com.vinaacademy.platform.configuration.AppConfig;
import com.vinaacademy.platform.exception.BadRequestException;
import com.vinaacademy.platform.feature.category.Category;
import com.vinaacademy.platform.feature.category.repository.CategoryRepository;
import com.vinaacademy.platform.feature.category.service.CategoryService;
import com.vinaacademy.platform.feature.common.helpers.SlugGeneratorHelper;
import com.vinaacademy.platform.feature.common.utils.SlugUtils;
import com.vinaacademy.platform.feature.course.dto.CourseCountStatusDto;
import com.vinaacademy.platform.feature.course.dto.CourseDetailsResponse;
import com.vinaacademy.platform.feature.course.dto.CourseDto;
import com.vinaacademy.platform.feature.course.dto.CourseRequest;
import com.vinaacademy.platform.feature.course.dto.CourseSearchRequest;
import com.vinaacademy.platform.feature.course.dto.CourseStatusRequest;
import com.vinaacademy.platform.feature.course.entity.Course;
import com.vinaacademy.platform.feature.course.enums.CourseStatus;
import com.vinaacademy.platform.feature.course.mapper.CourseMapper;
import com.vinaacademy.platform.feature.course.repository.CourseRepository;
import com.vinaacademy.platform.feature.course.repository.UserProgressRepository;
import com.vinaacademy.platform.feature.course.repository.specification.CourseSpecification;
import com.vinaacademy.platform.feature.enrollment.Enrollment;
import com.vinaacademy.platform.feature.enrollment.dto.EnrollmentProgressDto;
import com.vinaacademy.platform.feature.enrollment.mapper.EnrollmentMapper;
import com.vinaacademy.platform.feature.enrollment.repository.EnrollmentRepository;
import com.vinaacademy.platform.feature.instructor.CourseInstructor;
import com.vinaacademy.platform.feature.instructor.repository.CourseInstructorRepository;
import com.vinaacademy.platform.feature.lesson.dto.LessonDto;
import com.vinaacademy.platform.feature.lesson.entity.Lesson;
import com.vinaacademy.platform.feature.lesson.entity.UserProgress;
import com.vinaacademy.platform.feature.lesson.mapper.LessonMapper;
import com.vinaacademy.platform.feature.notification.dto.NotificationCreateDTO;
import com.vinaacademy.platform.feature.notification.dto.NotificationDTO;
import com.vinaacademy.platform.feature.notification.enums.NotificationType;
import com.vinaacademy.platform.feature.notification.service.NotificationService;
import com.vinaacademy.platform.feature.review.dto.CourseReviewDto;
import com.vinaacademy.platform.feature.review.mapper.CourseReviewMapper;
import com.vinaacademy.platform.feature.section.dto.SectionDto;
import com.vinaacademy.platform.feature.section.entity.Section;
import com.vinaacademy.platform.feature.section.mapper.SectionMapper;
import com.vinaacademy.platform.feature.section.repository.SectionRepository;
import com.vinaacademy.platform.feature.user.UserMapper;
import com.vinaacademy.platform.feature.user.UserRepository;
import com.vinaacademy.platform.feature.user.auth.helpers.SecurityHelper;
import com.vinaacademy.platform.feature.user.constant.AuthConstants;
import com.vinaacademy.platform.feature.user.entity.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service

public class CourseServiceImpl implements CourseService {

    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private SectionRepository sectionRepository;
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private CourseInstructorRepository courseInstructorRepository;
    @Autowired
    private UserProgressRepository lessonProgressRepository;
    @Autowired
    private SectionMapper sectionMapper;
    @Autowired
    private LessonMapper lessonMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SecurityHelper securityHelper;
    @Autowired
    private SlugGeneratorHelper slugGeneratorHelper;

    @Autowired
    private NotificationService notificationService;

    @Override
    public Boolean isInstructorOfCourse(UUID courseId, UUID instructorId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> BadRequestException.message("Khóa học không tồn tại"));
        User instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> BadRequestException.message("Giảng viên không tồn tại"));

        return course.getInstructors().stream()
                .anyMatch(courseInstructor -> courseInstructor.getInstructor().equals(instructor));
    }

    @Override
    public List<CourseDto> getCourses() {
        return courseRepository.findAll().stream().map(courseMapper::toDTO).toList();
    }

    @Override
    public Boolean existByCourseSlug(String slug) {
        Course course = courseRepository.findBySlug(slug)
                .orElse(null);
        if (course == null) {
            return false;
        }
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDetailsResponse getCourse(String slug) {
        Course course = courseRepository.findBySlug(slug)
                .orElseThrow(() -> BadRequestException.message("Khóa học không tồn tại"));

        // Use CourseMapper to create the base course details
        CourseDetailsResponse response = courseMapper.toCourseDetailsResponse(course);

        // Fetch and set instructors
        List<CourseInstructor> courseInstructors = courseInstructorRepository.findByCourse(course);
        response.setInstructors(courseInstructors.stream()
                .map(ci -> UserMapper.INSTANCE.toDto(ci.getInstructor()))
                .toList());

        // Find the owner instructor specifically
        courseInstructorRepository.findByCourseAndIsOwnerTrue(course)
                .ifPresent(owner -> response.setOwnerInstructor(UserMapper.INSTANCE.toDto(owner.getInstructor())));

        // Process sections with lessons using the common method
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

    @Override
    @Transactional(readOnly = true)
    public Page<CourseDetailsResponse> searchCourseDetails(CourseSearchRequest searchRequest, int page, int size,
                                                           String sortBy, String sortDirection) {
        Pageable pageable = createPageable(page, size, sortBy, sortDirection);

        Specification<Course> spec = Specification.where(CourseSpecification.hasKeyword(searchRequest.getKeyword()))
                .and(CourseSpecification.hasStatus(
                        searchRequest.getStatus() != null ? searchRequest.getStatus() : null))
                .and(CourseSpecification.dontHasStatus(CourseStatus.DRAFT))
                .and(CourseSpecification.hasCategory(searchRequest.getCategorySlug()))
                .and(CourseSpecification.hasLevel(searchRequest.getLevel()))
                .and(CourseSpecification.hasLanguage(searchRequest.getLanguage()))
                .and(CourseSpecification.hasMinPrice(searchRequest.getMinPrice()))
                .and(CourseSpecification.hasMaxPrice(searchRequest.getMaxPrice()))
                .and(CourseSpecification.hasMinRating(searchRequest.getMinRating()));

        Page<Course> coursePage = courseRepository.findAll(spec, pageable);

        return coursePage.map(course -> {
            CourseDetailsResponse response = courseMapper.toCourseDetailsResponse(course);

            // Set instructors
            List<CourseInstructor> courseInstructors = courseInstructorRepository.findByCourse(course);
            response.setInstructors(courseInstructors.stream()
                    .map(ci -> UserMapper.INSTANCE.toDto(ci.getInstructor()))
                    .toList());

            // Set owner instructor
            courseInstructorRepository.findByCourseAndIsOwnerTrue(course)
                    .ifPresent(owner -> response.setOwnerInstructor(UserMapper.INSTANCE.toDto(owner.getInstructor())));

            // Set sections and lessons
            List<SectionDto> sectionDtos = processSectionsAndLessons(course.getSections());
            response.setSections(sectionDtos);

            // Set reviews
            if (course.getCourseReviews() != null && !course.getCourseReviews().isEmpty()) {
                List<CourseReviewDto> reviewDtos = course.getCourseReviews().stream()
                        .map(CourseReviewMapper.INSTANCE::toDto)
                        .toList();
                response.setReviews(reviewDtos);
            }

            return response;
        });
    }

    @Override
    public CourseDto createCourse(CourseRequest request) {
        String baseSlug = StringUtils.isBlank(request.getSlug()) ?
                SlugUtils.toSlug(request.getName()) :
                request.getSlug();
        String slug = slugGeneratorHelper.generateSlug(baseSlug, s -> !courseRepository.existsBySlug(s));

        Category category = categoryRepository.findBySlug(request.getCategorySlug())
                .orElseThrow(() -> BadRequestException.message("Không tìm thấy danh mục"));
        Course course = Course.builder()
                .name(request.getName())
                .category(category)
                .description(request.getDescription())
                .image(request.getImage())
                .language(request.getLanguage())
                .level(request.getLevel())
                .price(request.getPrice())
                .rating(0)
                // .totalLesson(0)
                // .totalRating(0)
                // .totalSection(0)
                // .totalLesson(0)
                .slug(slug)
                .status(CourseStatus.DRAFT)
                .build();

        courseRepository.save(course);

        return courseMapper.toDTO(course);
    }

    @Override
    public CourseDto updateCourse(String slug, CourseRequest request) {
        Course course = courseRepository.findBySlug(slug)
                .orElseThrow(() -> BadRequestException.message("Khóa học không tồn tại"));
        User currentUser = securityHelper.getCurrentUser();
        if (!securityHelper.hasAnyRole(AuthConstants.ADMIN_ROLE, AuthConstants.STAFF_ROLE)
                && course.getInstructors().stream()
                .noneMatch(courseInstructor -> courseInstructor.getInstructor().equals(currentUser))) {
            throw BadRequestException.message("Người dùng không có quyền sửa khóa học này");
        }

        String oldSlug = course.getSlug();
        String newSlug = StringUtils.isBlank(request.getSlug()) ? oldSlug
                : SlugUtils.toSlug(request.getName());

        if (!oldSlug.equals(newSlug) && courseRepository.existsBySlug(newSlug)) {
            throw BadRequestException.message("Slug đã tồn tại");
        }

        Category category = categoryRepository.findBySlug(request.getCategorySlug())
                .orElseThrow(() -> BadRequestException.message("Không tìm thấy danh mục"));
        course.setName(request.getName());
        course.setSlug(newSlug);
        course.setCategory(category);
        course.setDescription(request.getDescription());
        course.setImage(request.getImage());
        course.setLanguage(request.getLanguage());
        course.setLevel(request.getLevel());
        course.setPrice(request.getPrice());
//        course.setStatus(request.getStatus());
        courseRepository.save(course);
        return courseMapper.toDTO(course);
    }

    @Override
    public void deleteCourse(String slug) {
        Course course = courseRepository.findBySlug(slug)
                .orElseThrow(() -> BadRequestException.message("Khóa học không tồn tại"));

        if (course.getTotalStudent() > 0) {
            throw BadRequestException.message("Khóa học đã có người đăng ký không thể xóa");
        }
        User currentUser = securityHelper.getCurrentUser();
        if (!securityHelper.hasAnyRole(AuthConstants.ADMIN_ROLE, AuthConstants.STAFF_ROLE, AuthConstants.INSTRUCTOR_ROLE)
        ) {
            throw BadRequestException.message("Người dùng không có quyền xóa khóa học này");
        }
        if (course.getInstructors().stream()
                .noneMatch(courseInstructor -> courseInstructor.getInstructor().equals(currentUser))) {
            throw BadRequestException.message("Người dùng không phải chủ sở hữu khóa học này");
        }

        courseRepository.delete(course);
    }

    @Override
    public List<CourseDto> getCoursesByCategory(String slug) {
        categoryRepository.findBySlug(slug).orElseThrow(() -> BadRequestException.message("Danh mục không tồn tại"));
        return courseRepository.findAllCourseByCategory(slug).stream().map(courseMapper::toDTO)
                .toList();
    }

    @Override
    public Page<CourseDto> getCoursesPaginated(int page, int size, String sortBy, String sortDirection,
                                               String categorySlug, double minRating) {
        Pageable pageable = createPageable(page, size, sortBy, sortDirection);
        Page<Course> coursePage;

        if (!StringUtils.isBlank(categorySlug) && minRating > 0) {
            coursePage = courseRepository.findByCategorySlugAndRatingGreaterThanEqual(categorySlug, minRating,
                    pageable);
        } else if (!StringUtils.isBlank(categorySlug)) {
            coursePage = courseRepository.findByCategorySlug(categorySlug, pageable);
        } else if (minRating > 0) {
            coursePage = courseRepository.findByRatingGreaterThanEqual(minRating, pageable);
        } else {
            coursePage = courseRepository.findAll(pageable);
        }
        return coursePage.map(courseMapper::toDTO);
    }

    @Override
    public Page<CourseDto> searchCourses(CourseSearchRequest searchRequest, int page, int size,
                                         String sortBy, String sortDirection) {
        Pageable pageable = createPageable(page, size, sortBy, sortDirection);

        // Build specification dynamically using the utility class
        Specification<Course> spec = Specification.where(CourseSpecification.hasKeyword(searchRequest.getKeyword()))
                .and(CourseSpecification.hasStatus(CourseStatus.PUBLISHED))
                .and(CourseSpecification.hasCategory(searchRequest.getCategorySlug()))
                .and(CourseSpecification.hasLevel(searchRequest.getLevel()))
                .and(CourseSpecification.hasLanguage(searchRequest.getLanguage()))
                .and(CourseSpecification.hasMinPrice(searchRequest.getMinPrice()))
                .and(CourseSpecification.hasMaxPrice(searchRequest.getMaxPrice()))
                .and(CourseSpecification.hasMinRating(searchRequest.getMinRating()));

        Page<Course> coursePage = courseRepository.findAll(spec, pageable);
        return coursePage.map(courseMapper::toDTO);
    }

    @Override
    public Page<CourseDto> getCoursesByInstructor(UUID instructorId, int page, int size,
                                                  String sortBy, String sortDirection) {
        Pageable pageable = createPageable(page, size, sortBy, sortDirection);

        User instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> BadRequestException.message("Không tìm thấy giảng viên"));

        Page<Course> coursePage = courseRepository.findByInstructorId(instructorId, pageable);
        return coursePage.map(courseMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseDto> searchInstructorCourses(
            UUID instructorId,
            CourseSearchRequest searchRequest,
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        Pageable pageable = createPageable(page, size, sortBy, sortDirection);

        // Tạo specification cho việc tìm kiếm
        Specification<Course> spec = Specification.where(CourseSpecification.hasInstructor(instructorId))
                .and(CourseSpecification.hasKeyword(searchRequest.getKeyword()))
                .and(CourseSpecification.hasStatus(searchRequest.getStatus()))
                .and(CourseSpecification.hasCategory(searchRequest.getCategorySlug()))
                .and(CourseSpecification.hasLevel(searchRequest.getLevel()))
                .and(CourseSpecification.hasLanguage(searchRequest.getLanguage()))
                .and(CourseSpecification.hasMinPrice(searchRequest.getMinPrice()))
                .and(CourseSpecification.hasMaxPrice(searchRequest.getMaxPrice()))
                .and(CourseSpecification.hasMinRating(searchRequest.getMinRating()));

        Page<Course> coursePage = courseRepository.findAll(spec, pageable);
        return coursePage.map(courseMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDto getCourseLearning(String slug) {
        Course course = courseRepository.findBySlug(slug)
                .orElseThrow(() -> BadRequestException.message("Khóa học không tồn tại"));
        CourseDto courseDto = courseMapper.toDTO(course);
        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw BadRequestException.message("Khóa học chưa được công khai");
        }

        User currentUser = securityHelper.getCurrentUser();
        // enrollment progress
        List<User> instructors = course.getInstructors().stream()
                .map(CourseInstructor::getInstructor)
                .toList();
        if (!securityHelper.hasAnyRole(AuthConstants.ADMIN_ROLE, AuthConstants.STAFF_ROLE)
                && !instructors.contains(currentUser)) {
            Enrollment courseEnrollment = enrollmentRepository.findByCourseAndUser(course, currentUser)
                    .orElseThrow(() -> BadRequestException.message("Người dùng không có quyền truy cập khóa học này"));
            courseDto.setProgress(EnrollmentMapper.INSTANCE.toDto2(courseEnrollment));
        } else {
            courseDto.setProgress(new EnrollmentProgressDto());
        }

        // sections + lessons
        List<Section> sections = course.getSections();

        // 1. First collect all lessons from all sections
        List<Lesson> allLessons = sections.stream()
                .flatMap(section -> section.getLessons().stream())
                .toList();

        // 2. Fetch all user progress records in a single query
        List<UserProgress> allUserProgress = lessonProgressRepository.findByUserAndLessonIn(currentUser, allLessons);

        // 3. Create a map for quick lookup: lessonId -> UserProgress
        Map<UUID, UserProgress> progressMap = allUserProgress.stream()
                .collect(Collectors.toMap(
                        progress -> progress.getLesson().getId(),
                        progress -> progress));

        // 4. Get sorted sections and lessons using the common method
        List<SectionDto> sectionDtos = processSectionsAndLessons(sections);

        // 5. Add user progress to each lesson
        for (SectionDto sectionDto : sectionDtos) {
            for (LessonDto lesson : sectionDto.getLessons()) {
                // Use the map to look up user progress instead of making individual queries
                UserProgress userProgress = progressMap.getOrDefault(
                        lesson.getId(),
                        new UserProgress());
                lesson.setCurrentUserProgress(userProgress);
            }
        }

        courseDto.setSections(sectionDtos);
        return courseDto;
    }

    /**
     * Process sections and their lessons, creating DTOs with sorted order
     *
     * @param sections List of section entities
     * @return List of section DTOs with ordered lessons
     */
    private List<SectionDto> processSectionsAndLessons(List<Section> sections) {
        if (sections == null || sections.isEmpty()) {
            return List.of();
        }

        return sections.stream()
                .sorted(java.util.Comparator.comparing(Section::getOrderIndex))
                .map(section -> {
                    SectionDto sectionDto = sectionMapper.toDto(section);
                    // Fetch and map lessons for each section
                    sectionDto.setLessons(section.getLessons().stream()
                            .sorted(java.util.Comparator.comparing(Lesson::getOrderIndex))
                            .map(lessonMapper::lessonToLessonDto)
                            .toList());
                    return sectionDto;
                })
                .toList();
    }

    /**
     * Create a pageable object based on sort parameters
     *
     * @param page          Page number
     * @param size          Page size
     * @param sortBy        Field to sort by
     * @param sortDirection Sort direction (asc/desc)
     * @return Configured Pageable object
     */
    private Pageable createPageable(int page, int size, String sortBy, String sortDirection) {
        Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDto getCourseById(UUID id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> BadRequestException.message("Khóa học không tồn tại"));
        return courseMapper.toDTO(course);
    }

    @Override
    @Transactional(readOnly = true)
    public String getCourseSlugById(UUID id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> BadRequestException.message("Khóa học không tồn tại"));
        return course.getSlug();
    }

    @Override
    public Boolean updateStatusCourse(CourseStatusRequest courseStatusRequest) {
        Course course = courseRepository.findBySlug(courseStatusRequest.getSlug())
                .orElseThrow(() -> BadRequestException.message("Khóa học không tồn tại"));
        if (courseStatusRequest.getStatus() == null)
            throw BadRequestException.message("Thiếu dữ liệu cần thiết");
        course.setStatus(courseStatusRequest.getStatus());
        if (course.getInstructors().isEmpty()) {
            throw BadRequestException.message("Khóa học không có giảng viên");
        }
        sendCourseNotification(course, course.getInstructors().get(0).getInstructor().getId());
        courseRepository.save(course);
        return true;
    }

    private void sendCourseNotification(Course course, UUID userId) {
        String title = String.format("Khóa học %s đã được cập nhật trạng thái", course.getName());
        String message = String.format("Khóa học %s đã được cập nhật trạng thái thành %s",
                course.getName(), course.getStatus().getValue());
        String url = String.format("%s%s%s%s",
                AppConfig.INSTANCE.getFrontendUrl(),
                "/instructor/courses/",
                course.getId(),
                "/content");
        NotificationCreateDTO request = NotificationCreateDTO.builder()
                .title(title)
                .content(message)
                .targetUrl(url)
                .userId(userId)
                .type(NotificationType.COURSE_APPROVAL)
                .build();
        notificationService.createNotification(request);
    }

    @Override
    public CourseCountStatusDto getCountCourses() {
        List<Object[]> statusCounts = courseRepository.countCoursesByStatus();

        long totalPublished = 0;
        long totalRejected = 0;
        long totalPending = 0;

        for (Object[] result : statusCounts) {
            CourseStatus status = (CourseStatus) result[0];
            long count = (long) result[1];

            if (status == CourseStatus.PUBLISHED) {
                totalPublished = count;
            } else if (status == CourseStatus.REJECTED) {
                totalRejected = count;
            } else if (status == CourseStatus.PENDING) {
                totalPending = count;
            }
        }
        CourseCountStatusDto countStatusDto = CourseCountStatusDto.builder()
                .totalPending(totalPending)
                .totalPublished(totalPublished)
                .totalRejected(totalRejected)
                .build();
        return countStatusDto;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseDto> getPublishedCoursesByInstructor(
            UUID instructorId,
            int page,
            int size,
            String sortBy,
            String sortDirection) {
        Pageable pageable = createPageable(page, size, sortBy, sortDirection);

        // Kiểm tra user có phải là instructor không
        User instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> BadRequestException.message("Không tìm thấy giảng viên"));

        // Kiểm tra xem user có role INSTRUCTOR không
        boolean isInstructor = instructor.getRoles().stream()
                .anyMatch(role -> role.getCode().equalsIgnoreCase(AuthConstants.INSTRUCTOR_ROLE));

        if (!isInstructor) {
            throw BadRequestException.message("Người dùng không phải là giảng viên");
        }

        // Lọc theo instructor và status = PUBLISHED
        Specification<Course> spec = Specification.where(CourseSpecification.hasInstructor(instructorId))
                .and(CourseSpecification.hasStatus(CourseStatus.PUBLISHED));

        Page<Course> coursePage = courseRepository.findAll(spec, pageable);
        return coursePage.map(courseMapper::toDTO);
    }

}
