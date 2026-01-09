package com.vinaacademy.platform.feature.migration.course;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinaacademy.platform.feature.category.Category;
import com.vinaacademy.platform.feature.course.entity.Course;
import com.vinaacademy.platform.feature.course.enums.CourseLevel;
import com.vinaacademy.platform.feature.course.enums.CourseStatus;
import com.vinaacademy.platform.feature.course.enums.LessonStatus;
import com.vinaacademy.platform.feature.course.repository.CourseRepository;
import com.vinaacademy.platform.feature.discussion.entity.Discussion;
import com.vinaacademy.platform.feature.discussion.repository.DiscussionRepository;
import com.vinaacademy.platform.feature.enrollment.Enrollment;
import com.vinaacademy.platform.feature.enrollment.enums.ProgressStatus;
import com.vinaacademy.platform.feature.enrollment.repository.EnrollmentRepository;
import com.vinaacademy.platform.feature.instructor.CourseInstructor;
import com.vinaacademy.platform.feature.instructor.repository.CourseInstructorRepository;
import com.vinaacademy.platform.feature.lesson.entity.Lesson;
import com.vinaacademy.platform.feature.migration.CategoryMigrationService;
import com.vinaacademy.platform.feature.migration.data.CourseData;
import com.vinaacademy.platform.feature.migration.data.DiscussionData;
import com.vinaacademy.platform.feature.migration.data.ReviewData;
import com.vinaacademy.platform.feature.migration.data.VideoData;
import com.vinaacademy.platform.feature.quiz.entity.Quiz;
import com.vinaacademy.platform.feature.quiz.repository.AnswerRepository;
import com.vinaacademy.platform.feature.quiz.repository.QuestionRepository;
import com.vinaacademy.platform.feature.quiz.repository.QuizRepository;
import com.vinaacademy.platform.feature.reading.Reading;
import com.vinaacademy.platform.feature.reading.repository.ReadingRepository;
import com.vinaacademy.platform.feature.revenue.entity.RevenueRecord;
import com.vinaacademy.platform.feature.revenue.enums.RevenueStatus;
import com.vinaacademy.platform.feature.revenue.repository.RevenueRecordRepository;
import com.vinaacademy.platform.feature.review.entity.CourseReview;
import com.vinaacademy.platform.feature.review.repository.CourseReviewRepository;
import com.vinaacademy.platform.feature.section.entity.Section;
import com.vinaacademy.platform.feature.section.repository.SectionRepository;
import com.vinaacademy.platform.feature.user.entity.User;
import com.vinaacademy.platform.feature.video.entity.Video;
import com.vinaacademy.platform.feature.video.enums.VideoStatus;
import com.vinaacademy.platform.feature.video.repository.VideoRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static com.vinaacademy.platform.feature.migration.data.ReadingData.selectAppropriateContent;
import static com.vinaacademy.platform.feature.migration.data.ReadingData.selectAppropriateReadingTitle;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseMockService {

    private final CategoryMigrationService categoryMigrationService;
    private final CourseRepository courseRepository;
    private final CourseReviewRepository courseReviewRepository;
    private final CourseInstructorRepository courseInstructorRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final DiscussionRepository discussionRepository;
    private final SectionRepository sectionRepository;
    private final ReadingRepository readingRepository;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final VideoRepository videoRepository;
    private final RevenueRecordRepository revenueRecordRepository;
    private final QuizMockDataService quizMockDataService;
    private final StudentMockService studentMockService;
    private final EntityManager entityManager;

    @Transactional
    public void createCoursesData(User instructor) throws IOException {
        // Read the JSON data from the file
        ObjectMapper objectMapper = new ObjectMapper();
        ClassPathResource resource = new ClassPathResource("data/categories-courses.json");
        try (InputStream inputStream = resource.getInputStream()) {
            JsonNode rootNode = objectMapper.readTree(inputStream);
            JsonNode categoriesNode = rootNode.get("categories");
            JsonNode coursesNode = rootNode.get("courses");

            // Create categories and build a map for quick lookup
            Map<String, Category> categoryMap = categoryMigrationService.createCategoriesFromJson(categoriesNode);

            // Create courses with empty sections
            createCoursesFromJson(coursesNode, categoryMap, instructor);

            log.info("Successfully created seed data from JSON file");
        }
    }

    /**
     * Create courses with sections and lessons from JSON
     */
    private void createCoursesFromJson(JsonNode coursesNode, Map<String, Category> categoryMap, User instructor) {
        int count = 0;
        for (JsonNode courseNode : coursesNode) {
            // Skip incomplete course entries
            if (!courseNode.has("id") || !courseNode.has("name") || !courseNode.has("slug") ||
                    !courseNode.has("description") || !courseNode.has("categoryId")) {
                continue;
            }

            try {
                String name = courseNode.get("name").asText();
                String description = CourseData.selectAppropriateDescription(name, courseNode.get("description").asText());
                String slug = courseNode.get("slug").asText();
                String image = courseNode.has("image") ? courseNode.get("image").asText() : "";

                // Get category
                String categoryId = courseNode.get("categoryId").asText();
                Category category = categoryMap.get(categoryId);
                if (category == null) {
                    log.warn("Category with ID {} not found for course {}", categoryId, name);
                    continue;
                }

                // Parse price - default to 0 if not present or invalid
                int randomInteger = (int) (Math.random() * 1000);
                randomInteger = randomInteger < 5 ? 0 : randomInteger * 1000;
                BigDecimal price = BigDecimal.valueOf(randomInteger);

                // Parse level - default to BEGINNER
                CourseLevel level = CourseLevel.BEGINNER;
                if (courseNode.has("level") && !courseNode.get("level").isNull()) {
                    try {
                        level = CourseLevel.valueOf(courseNode.get("level").asText());
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid level for course {}: {}", name, e.getMessage());
                    }
                }

                // Get language - default to Tiếng Việt
                String language = courseNode.has("language") ? courseNode.get("language").asText() : "Tiếng Việt";

                List<User> students = studentMockService.createRandomStudents();

                // Generate reviews
                List<CourseReview> reviews = new ArrayList<>();
                double totalRatingScore = 0;

                for (User student : students) {
                    // Random rating skewed towards positive (70% 4-5 stars)
                    int rating = ThreadLocalRandom.current().nextInt(1, 101) <= 70 ?
                            ThreadLocalRandom.current().nextInt(4, 6) :
                            ThreadLocalRandom.current().nextInt(1, 4);

                    String reviewContent = ReviewData.getRandomReview(rating);

                    CourseReview review = CourseReview.builder()
                            .rating(rating)
                            .review(reviewContent)
                            .user(student)
                            .build();
                    reviews.add(review);
                    totalRatingScore += rating;
                }

                double averageRating = reviews.isEmpty() ? 0.0 : totalRatingScore / reviews.size();
                averageRating = Math.round(averageRating * 10.0) / 10.0;

                // Create the course with calculated ratings
                Course course = Course.builder()
                        .name(name)
                        .description(description)
                        .slug(slug)
                        .image(image)
                        .price(price)
                        .level(level)
                        .status(CourseStatus.PUBLISHED)
                        .language(language)
                        .category(category)
                        .rating(averageRating)
                        .totalRating(reviews.size())
                        .totalStudent(students.size())
                        .totalSection(2)
                        .totalLesson(3)
                        .sections(new ArrayList<>())
                        .instructors(new ArrayList<>())
                        .build();

                course = courseRepository.save(course);

                // Save reviews
                for (CourseReview review : reviews) {
                    review.setCourse(course);
                    courseReviewRepository.save(review);
                }

                // Create enrollments for students
                for (User student : students) {
                    LocalDateTime startTime = LocalDateTime.now().minusDays(ThreadLocalRandom.current().nextLong(330));
                    double progress = ThreadLocalRandom.current().nextDouble(0, 100);
                    int completedLessons = (int) Math.round((progress / 100) * course.getTotalLesson());
                    Enrollment enrollment = Enrollment.builder()
                            .user(student)
                            .course(course)
                            .progressPercentage(progress)
                            .status(ProgressStatus.IN_PROGRESS)
                            .completedLessons(completedLessons)
                            .startAt(startTime)
                            .build();
                    enrollmentRepository.save(enrollment);

                    if (price.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal instructorPercent = BigDecimal.valueOf(0.7);
                        BigDecimal instructorEarning = price.multiply(instructorPercent);
                        BigDecimal platformFee = price.subtract(instructorEarning);

                        RevenueRecord revenueRecord = RevenueRecord.builder()
                                .courseId(course.getId())
                                .enrollmentId(enrollment.getId())
                                .paymentId(UUID.randomUUID())
                                .instructorId(instructor.getId())
                                .studentId(student.getId())
                                .totalAmount(price)
                                .instructorEarning(instructorEarning)
                                .platformFee(platformFee)
                                .instructorPercent(instructorPercent)
                                .status(RevenueStatus.ACTIVE)
                                .vnpayTxnRef(UUID.randomUUID().toString())
                                .vnpayResponseCode("00")
                                .vnpayTransactionNo(UUID.randomUUID().toString())
                                .vnpayOrderInfo("Payment for course " + name)
                                .vnpayAmount(price.multiply(BigDecimal.valueOf(100)))
                                .build();
                        revenueRecord.setCreatedDate(startTime);
                        revenueRecordRepository.save(revenueRecord);
                    }
                }

                // Assign instructor
                CourseInstructor courseInstructor = CourseInstructor.builder()
                        .instructor(instructor)
                        .course(course)
                        .isOwner(true)
                        .build();

                courseInstructorRepository.save(courseInstructor);

                // Create introduction section
                Section introSection = Section.createSection(
                        null,
                        course,
                        "Giới thiệu khóa học",
                        0,
                        null
                );
                sectionRepository.save(introSection);

                // Add welcome video in intro section
                Video welcomeVideo = Video.builder()
                        .title(VideoData.selectAppropriateVideoTitle(name, category.getName()))
                        .hlsPath(VideoData.selectAppropriateVideoHlsPath(name, category.getName()))
                        .section(introSection)
                        .free(true)
                        .orderIndex(0)
                        .author(instructor)
                        .status(VideoStatus.READY)
                        .lessonStatus(LessonStatus.PUBLISHED)
                        .build();
                welcomeVideo.setDuration(VideoData.VIDEO_DURATIONS.getOrDefault(welcomeVideo.getHlsPath(),
                        300.0));
                videoRepository.save(welcomeVideo);
                createDiscussionsForLesson(welcomeVideo, students);

                // Since we don't have actual videos, we just create the entity

                // Create content section
                Section contentSection = Section.createSection(
                        null,
                        course,
                        "Chuẩn bị hành trang học tập",
                        1,
                        null
                );
                sectionRepository.save(contentSection);

                // Add reading lesson to content section with actual content
                Reading reading = Reading.builder()
                        .title(selectAppropriateReadingTitle(name, category.getName()))
                        .section(contentSection)
                        .free(false)
                        .orderIndex(0)
                        .author(instructor)
                        .lessonStatus(LessonStatus.PUBLISHED)
                        .content(selectAppropriateContent(name, description, category.getName()))
                        .build();

                readingRepository.save(reading);
                createDiscussionsForLesson(reading, students);

                // Add a quiz lesson to content section
                Quiz quiz = Quiz.builder()
                        .title("Kiểm tra kiến thức")
                        .section(contentSection)
                        .free(false)
                        .orderIndex(1)
                        .author(instructor)
                        .description("Đánh giá sự hiểu biết của bạn về nội dung khóa học")
                        .passingScore(50.0)
                        .totalPoints(75.0)
                        .duration(15)
                        .lessonStatus(LessonStatus.PUBLISHED)
                        .randomizeQuestions(true)
                        .showCorrectAnswers(true)
                        .allowRetake(true)
                        .requirePassingScore(true)
                        .passingScore(70.0)
                        .timeLimit(15)
                        .build();

                quizRepository.save(quiz);

                // Add questions and answers to the quiz
                quizMockDataService.createQuizQuestions(quiz, name, category.getName());

                count++;
                if (count % 10 == 0) {
                    log.info("Created {} courses", count);
                }

            } catch (Exception e) {
                log.error("Error creating course: {}", e.getMessage(), e);
                // Clear the session to remove any failed entities from the persistence context
                // This prevents "AssertionFailure: Entry ... has a null identifier" on subsequent flushes
                entityManager.clear();
            }
        }
        log.info("Successfully created {} courses", count);
    }

    private void createDiscussionsForLesson(Lesson lesson, List<User> students) {
        if (students.isEmpty()) return;

        // Create 2-5 discussions per lesson
        int numDiscussions = ThreadLocalRandom.current().nextInt(2, 6);

        for (int i = 0; i < numDiscussions; i++) {
            User author = students.get(ThreadLocalRandom.current().nextInt(students.size()));
            
            Discussion question = Discussion.builder()
                    .lesson(lesson)
                    .user(author)
                    .comment(DiscussionData.getRandomQuestion())
                    .build();
            
            discussionRepository.save(question);

            // Add 0-3 replies
            int numReplies = ThreadLocalRandom.current().nextInt(0, 4);
            for (int j = 0; j < numReplies; j++) {
                User replyAuthor = students.get(ThreadLocalRandom.current().nextInt(students.size()));
                
                Discussion reply = Discussion.builder()
                        .lesson(lesson)
                        .user(replyAuthor)
                        .parentComment(question)
                        .comment(DiscussionData.getRandomReply())
                        .build();
                
                discussionRepository.save(reply);
            }
        }
    }
}
