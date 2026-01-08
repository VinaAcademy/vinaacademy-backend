package com.vinaacademy.platform.feature.migration.course;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinaacademy.platform.feature.category.Category;
import com.vinaacademy.platform.feature.course.entity.Course;
import com.vinaacademy.platform.feature.course.enums.CourseLevel;
import com.vinaacademy.platform.feature.course.enums.CourseStatus;
import com.vinaacademy.platform.feature.course.enums.LessonStatus;
import com.vinaacademy.platform.feature.course.repository.CourseRepository;
import com.vinaacademy.platform.feature.instructor.CourseInstructor;
import com.vinaacademy.platform.feature.instructor.repository.CourseInstructorRepository;
import com.vinaacademy.platform.feature.migration.CategoryMigrationService;
import com.vinaacademy.platform.feature.migration.data.CourseData;
import com.vinaacademy.platform.feature.migration.data.VideoData;
import com.vinaacademy.platform.feature.quiz.entity.Quiz;
import com.vinaacademy.platform.feature.quiz.repository.AnswerRepository;
import com.vinaacademy.platform.feature.quiz.repository.QuestionRepository;
import com.vinaacademy.platform.feature.quiz.repository.QuizRepository;
import com.vinaacademy.platform.feature.reading.Reading;
import com.vinaacademy.platform.feature.reading.repository.ReadingRepository;
import com.vinaacademy.platform.feature.section.entity.Section;
import com.vinaacademy.platform.feature.section.repository.SectionRepository;
import com.vinaacademy.platform.feature.user.entity.User;
import com.vinaacademy.platform.feature.video.entity.Video;
import com.vinaacademy.platform.feature.video.enums.VideoStatus;
import com.vinaacademy.platform.feature.video.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;

import static com.vinaacademy.platform.feature.migration.data.ReadingData.selectAppropriateContent;
import static com.vinaacademy.platform.feature.migration.data.ReadingData.selectAppropriateReadingTitle;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseMigrationService {

    private final CategoryMigrationService categoryMigrationService;
    private final CourseRepository courseRepository;
    private final CourseInstructorRepository courseInstructorRepository;
    private final SectionRepository sectionRepository;
    private final ReadingRepository readingRepository;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final VideoRepository videoRepository;
    private final QuizMockDataService quizMockDataService;

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

                // Create the course with zero students and ratings
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
                        .rating(0.0)
                        .totalRating(0)
                        .totalStudent(0)
                        .totalSection(2)
                        .totalLesson(3)
                        .sections(new ArrayList<>())
                        .instructors(new ArrayList<>())
                        .build();

                courseRepository.save(course);

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
                        .build();
                videoRepository.save(welcomeVideo);

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
            }
        }
        log.info("Successfully created {} courses", count);
    }
}
