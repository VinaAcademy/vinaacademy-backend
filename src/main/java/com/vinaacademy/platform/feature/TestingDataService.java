package com.vinaacademy.platform.feature;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinaacademy.platform.feature.category.Category;
import com.vinaacademy.platform.feature.category.repository.CategoryRepository;
import com.vinaacademy.platform.feature.common.utils.SlugUtils;
import com.vinaacademy.platform.feature.course.entity.Course;
import com.vinaacademy.platform.feature.course.enums.CourseLevel;
import com.vinaacademy.platform.feature.course.enums.CourseStatus;
import com.vinaacademy.platform.feature.course.repository.CourseRepository;
import com.vinaacademy.platform.feature.instructor.CourseInstructor;
import com.vinaacademy.platform.feature.instructor.repository.CourseInstructorRepository;
import com.vinaacademy.platform.feature.quiz.entity.Answer;
import com.vinaacademy.platform.feature.quiz.entity.Question;
import com.vinaacademy.platform.feature.quiz.entity.Quiz;
import com.vinaacademy.platform.feature.quiz.enums.QuestionType;
import com.vinaacademy.platform.feature.quiz.repository.AnswerRepository;
import com.vinaacademy.platform.feature.quiz.repository.QuestionRepository;
import com.vinaacademy.platform.feature.quiz.repository.QuizRepository;
import com.vinaacademy.platform.feature.reading.Reading;
import com.vinaacademy.platform.feature.reading.repository.ReadingRepository;
import com.vinaacademy.platform.feature.section.entity.Section;
import com.vinaacademy.platform.feature.section.repository.SectionRepository;
import com.vinaacademy.platform.feature.user.UserRepository;
import com.vinaacademy.platform.feature.user.constant.AuthConstants;
import com.vinaacademy.platform.feature.user.entity.User;
import com.vinaacademy.platform.feature.user.role.entity.Role;
import com.vinaacademy.platform.feature.user.role.repository.RoleRepository;
import com.vinaacademy.platform.feature.order_payment.repository.CouponRepository;
import com.vinaacademy.platform.feature.order_payment.entity.Coupon;
import com.vinaacademy.platform.feature.order_payment.enums.DiscountType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

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
    private final CouponRepository couponRepository;

    private final PasswordEncoder passwordEncoder;

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

        Role adminRole = roleRepository.findByCode(AuthConstants.ADMIN_ROLE);
        Role staffRole = roleRepository.findByCode(AuthConstants.STAFF_ROLE);
        Role instructorRole = roleRepository.findByCode(AuthConstants.INSTRUCTOR_ROLE);
        Role studentRole = roleRepository.findByCode(AuthConstants.STUDENT_ROLE);

        User admin = User.builder()
                .username("admin")
                .password("admin123")
                .email("locn562836@gmail.com")
                .enabled(true)
                .roles(Set.of(adminRole, studentRole))
                .build();
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));

        User staff = User.builder()
                .username("staff")
                .password("staff123")
                .email("huuloc2155@gmail.com")
                .enabled(true)
                .roles(Set.of(staffRole, studentRole))
                .build();
        staff.setPassword(passwordEncoder.encode(staff.getPassword()));

        User instructor = User.builder()
                .username("instructor")
                .password("instructor123")
                .email("linhpht263@outlook.com.vn")
                .enabled(true)
                .roles(Set.of(instructorRole, studentRole))
                .fullName("Linh Phan")
                .description("ABCxyz")
                .build();
        instructor.setPassword(passwordEncoder.encode(instructor.getPassword()));

        User student = User.builder()
                .username("student")
                .password("student123")
                .email("trihung987@gmail.com")
                .enabled(true)
                .roles(Set.of(studentRole))
                .build();
        student.setPassword(passwordEncoder.encode(student.getPassword()));

        userRepository.save(admin);
        userRepository.save(staff);
        userRepository.save(instructor);
        userRepository.save(student);
    }

    /**
     * Create seed data for coupons with various discount types and validity periods
     */
    @Transactional
    public void createCouponSeedData() {
        if (couponRepository.count() > 0) {
            log.info("Coupons already exist in the database, skipping coupon seed data creation");
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        // Active percentage coupons
        Coupon percentageCoupon1 = Coupon.builder()
                .code("WELCOME10")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(10))
                .startedAt(now.minusDays(7))
                .expiredAt(now.plusDays(30))
                .maxDiscountAmount(BigDecimal.valueOf(100000))
                .minOrderValue(BigDecimal.valueOf(50000))
                .usageLimit(100L)
                .usedCount(5L)
                .build();

        Coupon percentageCoupon2 = Coupon.builder()
                .code("STUDENT15")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(15))
                .startedAt(now.minusDays(3))
                .expiredAt(now.plusDays(60))
                .maxDiscountAmount(BigDecimal.valueOf(200000))
                .minOrderValue(BigDecimal.valueOf(100000))
                .usageLimit(200L)
                .usedCount(12L)
                .build();

        // Active fixed amount coupons
        Coupon fixedCoupon1 = Coupon.builder()
                .code("SAVE50K")
                .discountType(DiscountType.FIXED_AMOUNT)
                .discountValue(BigDecimal.valueOf(50000))
                .startedAt(now.minusDays(5))
                .expiredAt(now.plusDays(45))
                .minOrderValue(BigDecimal.valueOf(200000))
                .usageLimit(50L)
                .usedCount(8L)
                .build();

        Coupon fixedCoupon2 = Coupon.builder()
                .code("NEWUSER25K")
                .discountType(DiscountType.FIXED_AMOUNT)
                .discountValue(BigDecimal.valueOf(25000))
                .startedAt(now.minusDays(1))
                .expiredAt(now.plusDays(90))
                .minOrderValue(BigDecimal.valueOf(100000))
                .usageLimit(500L)
                .usedCount(45L)
                .build();

        // Premium coupons with higher values
        Coupon premiumCoupon = Coupon.builder()
                .code("PREMIUM20")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(20))
                .startedAt(now.minusDays(10))
                .expiredAt(now.plusDays(15))
                .maxDiscountAmount(BigDecimal.valueOf(500000))
                .minOrderValue(BigDecimal.valueOf(500000))
                .usageLimit(20L)
                .usedCount(3L)
                .build();

        // Limited time flash sale coupon
        Coupon flashSale = Coupon.builder()
                .code("FLASH30")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(30))
                .startedAt(now.minusHours(2))
                .expiredAt(now.plusHours(22))
                .maxDiscountAmount(BigDecimal.valueOf(300000))
                .minOrderValue(BigDecimal.valueOf(300000))
                .usageLimit(10L)
                .usedCount(7L)
                .build();

        // Expired coupons for testing
        Coupon expiredCoupon = Coupon.builder()
                .code("EXPIRED10")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(10))
                .startedAt(now.minusDays(30))
                .expiredAt(now.minusDays(5))
                .maxDiscountAmount(BigDecimal.valueOf(50000))
                .minOrderValue(BigDecimal.valueOf(50000))
                .usageLimit(100L)
                .usedCount(85L)
                .build();

        // Future coupon (not yet active)
        Coupon futureCoupon = Coupon.builder()
                .code("FUTURE25")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(25))
                .startedAt(now.plusDays(7))
                .expiredAt(now.plusDays(37))
                .maxDiscountAmount(BigDecimal.valueOf(250000))
                .minOrderValue(BigDecimal.valueOf(200000))
                .usageLimit(50L)
                .usedCount(0L)
                .build();

        // Fully used coupon
        Coupon fullyUsedCoupon = Coupon.builder()
                .code("SOLD100K")
                .discountType(DiscountType.FIXED_AMOUNT)
                .discountValue(BigDecimal.valueOf(100000))
                .startedAt(now.minusDays(15))
                .expiredAt(now.plusDays(15))
                .minOrderValue(BigDecimal.valueOf(500000))
                .usageLimit(5L)
                .usedCount(5L)
                .build();

        // Unlimited usage coupon
        Coupon unlimitedCoupon = Coupon.builder()
                .code("ALWAYS5")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(5))
                .startedAt(now.minusDays(365))
                .expiredAt(now.plusDays(365))
                .maxDiscountAmount(BigDecimal.valueOf(50000))
                .minOrderValue(BigDecimal.valueOf(50000))
                .usageLimit(null) // Unlimited
                .usedCount(150L)
                .build();

        // Save all coupons
        couponRepository.save(percentageCoupon1);
        couponRepository.save(percentageCoupon2);
        couponRepository.save(fixedCoupon1);
        couponRepository.save(fixedCoupon2);
        couponRepository.save(premiumCoupon);
        couponRepository.save(flashSale);
        couponRepository.save(expiredCoupon);
        couponRepository.save(futureCoupon);
        couponRepository.save(fullyUsedCoupon);
        couponRepository.save(unlimitedCoupon);

        log.info("Successfully created 10 coupon seed data entries");
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

        // Create coupon seed data along with other data
        createCouponSeedData();

        try {
            // Get instructor user or create one if not exists
            User instructor = userRepository.findByUsername("instructor")
                    .orElseGet(() -> {
                        User newInstructor = User.builder()
                                .username("instructor")
                                .password(passwordEncoder.encode("instructor123"))
                                .email("instructor@example.com")
                                .enabled(true)
                                .roles(Set.of(roleRepository.findByCode(AuthConstants.INSTRUCTOR_ROLE)))
                                .build();
                        return userRepository.save(newInstructor);
                    });

            // Read the JSON data from the file
            ObjectMapper objectMapper = new ObjectMapper();
            ClassPathResource resource = new ClassPathResource("data/categories-courses.json");
            try (InputStream inputStream = resource.getInputStream()) {
                JsonNode rootNode = objectMapper.readTree(inputStream);
                JsonNode categoriesNode = rootNode.get("categories");
                JsonNode coursesNode = rootNode.get("courses");

                // Create categories and build a map for quick lookup
                Map<String, Category> categoryMap = createCategoriesFromJson(categoriesNode);

                // Create courses with empty sections
                createCoursesFromJson(coursesNode, categoryMap, instructor);

                log.info("Successfully created seed data from JSON file");
            }
        } catch (IOException e) {
            log.error("Error reading categories-courses.json", e);
        } catch (Exception e) {
            log.error("Error creating seed data", e);
        }
    }

    /**
     * Create categories from JSON and return a map of id to Category object
     */
    private Map<String, Category> createCategoriesFromJson(JsonNode categoriesNode) {
        Map<String, Category> categoryMap = new HashMap<>();
        Map<String, Category> tempMap = new HashMap<>();
        Set<String> existingSlugs = new HashSet<>(); // Keep track of existing slugs

        // First pass: create all categories without parent relationships
        for (JsonNode categoryNode : categoriesNode) {
            String id = categoryNode.get("id").asText();
            String name = categoryNode.get("name").asText();

            // Generate slug using SlugUtils if needed, or use the one from JSON
            String originalSlug;
            if (categoryNode.has("slug") && !categoryNode.get("slug").isNull()) {
                originalSlug = categoryNode.get("slug").asText();

                if (existingSlugs.contains(originalSlug)) {
                    originalSlug = SlugUtils.toSlug(name);
                }
            } else {
                originalSlug = SlugUtils.toSlug(name);
            }

            // Check for duplicate slug and make it unique if needed
            String slug = ensureUniqueSlug(originalSlug, existingSlugs);

            Category category = Category.builder()
                    .name(name)
                    .slug(slug)
                    .build();

            tempMap.put(id, category);
        }

        // Second pass: set parent relationships and save categories
        for (JsonNode categoryNode : categoriesNode) {
            String id = categoryNode.get("id").asText();
            JsonNode parentIdNode = categoryNode.get("parentId");

            Category category = tempMap.get(id);

            if (parentIdNode != null && !parentIdNode.isNull()) {
                String parentId = parentIdNode.asText();
                Category parentCategory = tempMap.get(parentId);
                category.setParent(parentCategory);
            }

            categoryRepository.save(category);
            categoryMap.put(id, category);
        }

        log.info("Created {} categories from JSON", categoryMap.size());
        return categoryMap;
    }

    /**
     * Helper method to ensure slug uniqueness
     */
    private String ensureUniqueSlug(String originalSlug, Set<String> existingSlugs) {
        String slug = originalSlug;
        int counter = 1;
        // If the slug already exists, append a counter until we have a unique slug
        while (existingSlugs.contains(slug) || categoryRepository.findBySlug(slug).isPresent()) {
            slug = originalSlug + "-" + counter;
            counter++;
        }
        existingSlugs.add(slug);
        return slug;
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
                String description = generateDefaultDescription(name);
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
                        .totalLesson(2)
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
//                Video welcomeVideo = Video.builder()
//                        .title("Chào mừng đến với khóa học")
//                        .section(introSection)
//                        .free(true)
//                        .orderIndex(0)
//                        .author(instructor)
//                        .status(VideoStatus.PROCESSING)
//                        .build();

                // Since we don't have actual videos, we just create the entity

                // Create content section
                Section contentSection = Section.createSection(
                        null,
                        course,
                        "Nội dung khóa học",
                        1,
                        null
                );
                sectionRepository.save(contentSection);

                // Add reading lesson to content section with actual content
                Reading reading = Reading.builder()
                        .title("Thời lượng khóa học: Tổng số " + generateRandomDuration() + " giờ")
                        .section(contentSection)
                        .free(false)
                        .orderIndex(0)
                        .author(instructor)
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
                        .passingScore(70.0)
                        .totalPoints(100.0)
                        .duration(15)
                        .randomizeQuestions(true)
                        .showCorrectAnswers(true)
                        .allowRetake(true)
                        .requirePassingScore(true)
                        .passingScore(70.0)
                        .timeLimit(15)
                        .build();

                quizRepository.save(quiz);

                // Add questions and answers to the quiz
                createQuizQuestions(quiz, name, category.getName());

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

    /**
     * Generate a random duration for a course between 2 and 50 hours
     */
    private String generateRandomDuration() {
        Random random = new Random();
        int hours = random.nextInt(48) + 2; // 2 to 50 hours

        if (random.nextBoolean()) {
            // Sometimes add half hours
            return hours + ",5";
        } else {
            return String.valueOf(hours);
        }
    }

    private String generateReadingContent(String courseName, String courseDescription, String categoryName) {
        StringBuilder content = new StringBuilder();

        // Main heading
        content.append("<h1>").append(courseName).append("</h1>");

        // Course description
        content.append("<p>").append(courseDescription).append("</p>");

        // Course overview
        content.append("<h2>Tổng quan khóa học</h2>");
        content.append("<p>Khóa học toàn diện này trong lĩnh vực <strong>").append(categoryName)
                .append("</strong> sẽ hướng dẫn bạn qua tất cả các khái niệm và kỹ năng thực tế cần thiết để thành thạo trong lĩnh vực này.</p>");

        // What you'll learn
        content.append("<h2>Bạn sẽ học được gì</h2>");
        content.append("<ul>");
        content.append("<li>Khái niệm và nguyên lý cơ bản của ").append(categoryName).append("</li>");
        content.append("<li>Kỹ năng và kỹ thuật thực hành thông qua các bài tập</li>");
        content.append("<li>Chiến lược nâng cao cho ứng dụng thực tế</li>");
        content.append("<li>Quy tắc thực hành tốt và tiêu chuẩn ngành</li>");
        content.append("<li>Giải quyết vấn đề và tư duy phản biện trong bối cảnh của ").append(categoryName).append("</li>");
        content.append("</ul>");

        // Course structure
        content.append("<h2>Cấu trúc khóa học</h2>");
        content.append("<p>Khóa học này được chia thành nhiều module, mỗi module tập trung vào các khía cạnh cụ thể của chủ đề. ");
        content.append("Bạn sẽ được học lý thuyết sau đó là các bài tập thực hành để củng cố kiến thức.</p>");

        // Prerequisites
        content.append("<h2>Điều kiện tiên quyết</h2>");
        content.append("<p>Mặc dù khóa học này được thiết kế để dễ tiếp cận với người mới bắt đầu, việc có một số kiến thức cơ bản trong các lĩnh vực sau sẽ có lợi:</p>");
        content.append("<ul>");
        content.append("<li>Kỹ năng máy tính cơ bản</li>");
        content.append("<li>Hiểu biết nền tảng về các khái niệm ").append(categoryName).append("</li>");
        content.append("<li>Sự nhiệt tình và sẵn lòng học hỏi!</li>");
        content.append("</ul>");

        // Assessment methods
        content.append("<h2>Phương pháp đánh giá</h2>");
        content.append("<p>Tiến độ của bạn sẽ được đánh giá thông qua:</p>");
        content.append("<ul>");
        content.append("<li>Bài kiểm tra cuối mỗi phần</li>");
        content.append("<li>Bài tập thực hành</li>");
        content.append("<li>Một bài đánh giá toàn diện cuối cùng</li>");
        content.append("</ul>");

        // Closing
        content.append("<p>Chúng tôi rất vui mừng khi có bạn tham gia vào hành trình học tập này. Hãy bắt đầu!</p>");

        // Add current date info
        content.append("<p><em>Khóa học được cập nhật vào: 2025-05-07</em></p>");

        return content.toString();
    }

    /**
     * Generates a default course description based on the course name in HTML format
     *
     * @param courseName The name of the course
     * @return A default description for the course in HTML
     */
    private String generateDefaultDescription(String courseName) {
        StringBuilder description = new StringBuilder();

        description.append("<p>Chào mừng bạn đến với khóa học \"<strong>").append(courseName).append("</strong>\"! ");
        description.append("Khóa học này được thiết kế để cung cấp cho bạn những kiến thức và kỹ năng toàn diện ");
        description.append("giúp bạn trở nên thành thạo trong lĩnh vực này. ");
        description.append("Từ các nguyên lý cơ bản đến các kỹ thuật nâng cao, khóa học sẽ đồng hành cùng bạn ");
        description.append("trong suốt hành trình học tập và phát triển chuyên môn. ");
        description.append("Với sự kết hợp giữa lý thuyết và thực hành, bạn sẽ được trang bị đầy đủ công cụ ");
        description.append("để ứng dụng hiệu quả trong môi trường thực tế sau khi hoàn thành khóa học.</p>");

        return description.toString();
    }

    /**
     * Create quiz questions and answers for a quiz with Vietnamese content
     */
    private void createQuizQuestions(Quiz quiz, String courseName, String categoryName) {
        // Create multiple choice question
        Question multipleChoiceQuestion = Question.builder()
                .quiz(quiz)
                .questionText("Những thành phần chính nào được đề cập trong khóa học " + categoryName + " này? (Chọn tất cả đáp án đúng)")
                .explanation("Đây là những thành phần chính mà chúng tôi tập trung trong chương trình giảng dạy.")
                .point(25.0)
                .questionType(QuestionType.MULTIPLE_CHOICE)
                .build();

        questionRepository.save(multipleChoiceQuestion);

        // Add answers for multiple choice question
        Answer answer1 = Answer.builder()
                .question(multipleChoiceQuestion)
                .answerText("Nền tảng lý thuyết và nguyên lý")
                .isCorrect(true)
                .build();

        Answer answer2 = Answer.builder()
                .question(multipleChoiceQuestion)
                .answerText("Kỹ thuật thực hành và ứng dụng")
                .isCorrect(true)
                .build();

        Answer answer3 = Answer.builder()
                .question(multipleChoiceQuestion)
                .answerText("Phương pháp giải quyết vấn đề nâng cao")
                .isCorrect(true)
                .build();

        Answer answer4 = Answer.builder()
                .question(multipleChoiceQuestion)
                .answerText("Các chủ đề không liên quan không được đề cập trong khóa học này")
                .isCorrect(false)
                .build();

        answerRepository.save(answer1);
        answerRepository.save(answer2);
        answerRepository.save(answer3);
        answerRepository.save(answer4);

        // Create single choice question
        Question singleChoiceQuestion = Question.builder()
                .quiz(quiz)
                .questionText("Mục tiêu chính của khóa học " + courseName + " là gì?")
                .explanation("Hiểu rõ mục tiêu chính giúp định hướng kỳ vọng học tập của bạn.")
                .point(25.0)
                .questionType(QuestionType.SINGLE_CHOICE)
                .build();

        questionRepository.save(singleChoiceQuestion);

        // Add answers for single choice question
        Answer singleAnswer1 = Answer.builder()
                .question(singleChoiceQuestion)
                .answerText("Giảng dạy kỹ năng và kiến thức " + categoryName + " toàn diện")
                .isCorrect(true)
                .build();

        Answer singleAnswer2 = Answer.builder()
                .question(singleChoiceQuestion)
                .answerText("Chỉ tập trung vào khía cạnh lý thuyết mà không có ứng dụng thực tế")
                .isCorrect(false)
                .build();

        Answer singleAnswer3 = Answer.builder()
                .question(singleChoiceQuestion)
                .answerText("Cung cấp giải trí mà không có nội dung giáo dục")
                .isCorrect(false)
                .build();

        Answer singleAnswer4 = Answer.builder()
                .question(singleChoiceQuestion)
                .answerText("Giảng dạy các chủ đề không liên quan không được đề cập trong mô tả khóa học")
                .isCorrect(false)
                .build();

        answerRepository.save(singleAnswer1);
        answerRepository.save(singleAnswer2);
        answerRepository.save(singleAnswer3);
        answerRepository.save(singleAnswer4);

        // Create true/false question
        Question trueFalseQuestion = Question.builder()
                .quiz(quiz)
                .questionText("Khóa học này bao gồm cả kiến thức lý thuyết và ứng dụng thực tế.")
                .explanation("Đây là khía cạnh cơ bản trong phương pháp giảng dạy của chúng tôi.")
                .point(25.0)
                .questionType(QuestionType.TRUE_FALSE)
                .build();

        questionRepository.save(trueFalseQuestion);

        // Add answers for true/false question
        Answer trueAnswer = Answer.builder()
                .question(trueFalseQuestion)
                .answerText("Đúng")
                .isCorrect(true)
                .build();

        Answer falseAnswer = Answer.builder()
                .question(trueFalseQuestion)
                .answerText("Sai")
                .isCorrect(false)
                .build();

        answerRepository.save(trueAnswer);
        answerRepository.save(falseAnswer);

        // Create programming-related question if the category is related to programming
        if (categoryName.contains("lập trình") || categoryName.contains("Lập trình") ||
                categoryName.contains("ngôn ngữ") || categoryName.contains("IT") ||
                categoryName.contains("phần mềm") || categoryName.contains("web") ||
                categoryName.contains("CNTT") || categoryName.contains("phát triển")) {

            Question programmingQuestion = Question.builder()
                    .quiz(quiz)
                    .questionText("Đâu là một cách hiệu quả để kiểm tra lỗi trong quá trình phát triển phần mềm?")
                    .explanation("Kiểm tra lỗi và gỡ lỗi là kỹ năng quan trọng trong phát triển phần mềm.")
                    .point(25.0)
                    .questionType(QuestionType.SINGLE_CHOICE)
                    .build();

            questionRepository.save(programmingQuestion);

            Answer pAnswer1 = Answer.builder()
                    .question(programmingQuestion)
                    .answerText("Kiểm thử đơn vị (Unit testing)")
                    .isCorrect(true)
                    .build();

            Answer pAnswer2 = Answer.builder()
                    .question(programmingQuestion)
                    .answerText("Chỉ dựa vào đánh giá trực quan")
                    .isCorrect(false)
                    .build();

            Answer pAnswer3 = Answer.builder()
                    .question(programmingQuestion)
                    .answerText("Không cần kiểm tra cho đến khi hoàn thành toàn bộ dự án")
                    .isCorrect(false)
                    .build();

            Answer pAnswer4 = Answer.builder()
                    .question(programmingQuestion)
                    .answerText("Xóa code và viết lại mỗi khi gặp lỗi")
                    .isCorrect(false)
                    .build();

            answerRepository.save(pAnswer1);
            answerRepository.save(pAnswer2);
            answerRepository.save(pAnswer3);
            answerRepository.save(pAnswer4);

        } else {
            // Create essay question for non-programming courses
            Question essayQuestion = Question.builder()
                    .quiz(quiz)
                    .questionText("Giải thích cách bạn dự định áp dụng kiến thức từ khóa học " + categoryName + " này vào các dự án cá nhân hoặc công việc của bạn.")
                    .explanation("Điều này giúp chúng tôi hiểu mục tiêu học tập và kỳ vọng của bạn.")
                    .point(25.0)
                    .questionType(QuestionType.TEXT)
                    .build();

            questionRepository.save(essayQuestion);
        }

        // Update quiz total points based on questions
        double totalPoints = 100.0; // All questions add up to 100 points
        quiz.setTotalPoints(totalPoints);
        quizRepository.save(quiz);
    }

    /**
     * Creates programming-specific reading content formatted for Tiptap editor
     *
     * @param courseName   The name of the course
     * @param categoryName The category name
     * @return HTML content compatible with Tiptap editor
     */
    private String createProgrammingReadingContent(String courseName, String categoryName) {
        StringBuilder content = new StringBuilder();

        // Main heading
        content.append("<h1>").append(courseName).append("</h1>");

        // Introduction section
        content.append("<div>");
        content.append("<h2>Giới thiệu về lập trình trong ").append(categoryName).append("</h2>");
        content.append("<p>Trong thế giới công nghệ ngày nay, việc thành thạo các kỹ năng lập trình là vô cùng quan trọng. ");
        content.append("Khóa học này sẽ giúp bạn hiểu rõ và ứng dụng thành thạo những khái niệm lập trình quan trọng.</p>");
        content.append("</div>");

        // Data structures and algorithms section
        content.append("<div>");
        content.append("<h2>Cấu trúc dữ liệu và thuật toán</h2>");
        content.append("<pre><code># Ví dụ về thuật toán sắp xếp nhanh (Quick sort)\n");
        content.append("def quick_sort(arr):\n");
        content.append("    if len(arr) <= 1:\n");
        content.append("        return arr\n");
        content.append("    pivot = arr[len(arr) // 2]\n");
        content.append("    left = [x for x in arr if x < pivot]\n");
        content.append("    middle = [x for x in arr if x == pivot]\n");
        content.append("    right = [x for x in arr if x > pivot]\n");
        content.append("    return quick_sort(left) + middle + quick_sort(right)\n");
        content.append("\n");
        content.append("# Sử dụng ví dụ\n");
        content.append("mang_so = [3, 6, 8, 10, 1, 2, 1]\n");
        content.append("mang_da_sap_xep = quick_sort(mang_so)\n");
        content.append("print(\"Kết quả: \", mang_da_sap_xep)</code></pre>");
        content.append("</div>");

        // OOP principles section
        content.append("<div>");
        content.append("<h2>Nguyên lý lập trình hướng đối tượng</h2>");
        content.append("<p>Lập trình hướng đối tượng (OOP) là một phương pháp lập trình dựa trên khái niệm về \"đối tượng\".</p>");
        content.append("<p><strong>Các nguyên tắc cơ bản:</strong></p>");
        content.append("<ol>");
        content.append("<li><strong>Tính đóng gói (Encapsulation)</strong> - Ẩn dữ liệu thực thi chi tiết</li>");
        content.append("<li><strong>Tính kế thừa (Inheritance)</strong> - Cho phép lớp con kế thừa từ lớp cha</li>");
        content.append("<li><strong>Tính đa hình (Polymorphism)</strong> - Cho phép các đối tượng khác nhau phản ứng khác nhau với cùng một thông điệp</li>");
        content.append("<li><strong>Tính trừu tượng (Abstraction)</strong> - Ẩn sự phức tạp thông qua các giao diện đơn giản</li>");
        content.append("</ol>");
        content.append("</div>");

        // Java example section
        content.append("<div>");
        content.append("<h2>Ví dụ về lớp và đối tượng trong Java</h2>");
        content.append("<pre><code>public class NhanVien {\n");
        content.append("    // Thuộc tính\n");
        content.append("    private String hoTen;\n");
        content.append("    private int tuoi;\n");
        content.append("    private double luong;\n\n");
        content.append("    // Constructor\n");
        content.append("    public NhanVien(String hoTen, int tuoi, double luong) {\n");
        content.append("        this.hoTen = hoTen;\n");
        content.append("        this.tuoi = tuoi;\n");
        content.append("        this.luong = luong;\n");
        content.append("    }\n\n");
        content.append("    // Phương thức\n");
        content.append("    public void hienThiThongTin() {\n");
        content.append("        System.out.println(\"Họ tên: \" + hoTen);\n");
        content.append("        System.out.println(\"Tuổi: \" + tuoi);\n");
        content.append("        System.out.println(\"Lương: \" + luong);\n");
        content.append("    }\n");
        content.append("}</code></pre>");
        content.append("</div>");

        // References section
        content.append("<div>");
        content.append("<h2>Tài liệu tham khảo</h2>");
        content.append("<ul>");
        content.append("<li>Clean Code - Robert C. Martin</li>");
        content.append("<li>Design Patterns - Gang of Four</li>");
        content.append("<li>Effective Java - Joshua Bloch</li>");
        content.append("<li>Head First Design Patterns</li>");
        content.append("</ul>");
        content.append("</div>");

        // Exercises section
        content.append("<div>");
        content.append("<h2>Bài tập thực hành</h2>");
        content.append("<ol>");
        content.append("<li>Tạo một ứng dụng quản lý sinh viên đơn giản</li>");
        content.append("<li>Áp dụng các nguyên tắc OOP vào dự án của bạn</li>");
        content.append("<li>Tối ưu hóa một thuật toán sắp xếp để cải thiện hiệu suất</li>");
        content.append("</ol>");
        content.append("<p style=\"text-align: center;\"><strong>Chúc bạn học tập hiệu quả!</strong></p>");
        content.append("</div>");

        // Add author and date info - useful for course versioning
        content.append("<div>");
        content.append("<p><em>Tác giả: lochuung</em></p>");
        content.append("<p><em>Cập nhật lần cuối: 2025-05-07</em></p>");
        content.append("</div>");

        return content.toString();
    }

    /**
     * Creates business or finance specific reading content formatted for Tiptap editor
     *
     * @param courseName   The name of the course
     * @param categoryName The category name
     * @return HTML content compatible with Tiptap editor
     */
    private String createBusinessReadingContent(String courseName, String categoryName) {
        StringBuilder content = new StringBuilder();

        // Main heading
        content.append("<h1>").append(courseName).append("</h1>");

        // Overview section
        content.append("<div>");
        content.append("<h2>Tổng quan về ").append(categoryName).append("</h2>");
        content.append("<p>Trong môi trường kinh doanh cạnh tranh ngày nay, việc hiểu rõ và áp dụng các nguyên tắc quản lý ");
        content.append("và chiến lược kinh doanh hiệu quả là chìa khóa để thành công. Khóa học này cung cấp những kiến thức ");
        content.append("thiết yếu giúp bạn vững vàng trong lĩnh vực ").append(categoryName).append(".</p>");
        content.append("</div>");

        // Market analysis section
        content.append("<div>");
        content.append("<h2>Phân tích thị trường</h2>");
        content.append("<p>Phân tích thị trường là một quy trình thiết yếu giúp doanh nghiệp hiểu rõ về:</p>");
        content.append("<ul>");
        content.append("<li>Xu hướng tiêu dùng hiện tại</li>");
        content.append("<li>Hành vi của khách hàng</li>");
        content.append("<li>Chiến lược của đối thủ cạnh tranh</li>");
        content.append("<li>Cơ hội và thách thức mới nổi</li>");
        content.append("</ul>");
        content.append("</div>");

        // SWOT matrix section
        content.append("<div>");
        content.append("<h2>Ma trận SWOT</h2>");
        content.append("<table>");
        content.append("<tr>");
        content.append("<th></th>");
        content.append("<th>Tích cực</th>");
        content.append("<th>Tiêu cực</th>");
        content.append("</tr>");
        content.append("<tr>");
        content.append("<th>Nội bộ</th>");
        content.append("<td><strong>Điểm mạnh</strong><br>");
        content.append("- Nguồn lực độc đáo<br>");
        content.append("- Công nghệ tiên tiến<br>");
        content.append("- Đội ngũ chuyên nghiệp");
        content.append("</td>");
        content.append("<td><strong>Điểm yếu</strong><br>");
        content.append("- Thiếu nguồn vốn<br>");
        content.append("- Quy trình chưa tối ưu<br>");
        content.append("- Hạn chế về năng lực");
        content.append("</td>");
        content.append("</tr>");
        content.append("<tr>");
        content.append("<th>Bên ngoài</th>");
        content.append("<td><strong>Cơ hội</strong><br>");
        content.append("- Thị trường mới<br>");
        content.append("- Đối tác tiềm năng<br>");
        content.append("- Xu hướng mới");
        content.append("</td>");
        content.append("<td><strong>Thách thức</strong><br>");
        content.append("- Đối thủ cạnh tranh<br>");
        content.append("- Quy định pháp luật<br>");
        content.append("- Biến động kinh tế");
        content.append("</td>");
        content.append("</tr>");
        content.append("</table>");
        content.append("</div>");

        // Pricing strategy section
        content.append("<div>");
        content.append("<h2>Chiến lược định giá</h2>");
        content.append("<p>Việc xây dựng chiến lược định giá hiệu quả là yếu tố then chốt quyết định thành công của doanh nghiệp. ");
        content.append("Dưới đây là một số phương pháp phổ biến:</p>");

        content.append("<div>");
        content.append("<h3>1. Định giá dựa trên chi phí</h3>");
        content.append("<p>Tính toán chi phí sản xuất và thêm phần lợi nhuận mong muốn</p>");
        content.append("</div>");

        content.append("<div>");
        content.append("<h3>2. Định giá dựa trên giá trị</h3>");
        content.append("<p>Xác định mức giá dựa trên giá trị mà khách hàng nhận được</p>");
        content.append("</div>");

        content.append("<div>");
        content.append("<h3>3. Định giá cạnh tranh</h3>");
        content.append("<p>Đặt giá dựa trên mức giá của đối thủ cạnh tranh</p>");
        content.append("</div>");

        content.append("<div>");
        content.append("<h3>4. Định giá theo phân khúc</h3>");
        content.append("<p>Áp dụng các mức giá khác nhau cho các phân khúc khách hàng khác nhau</p>");
        content.append("</div>");
        content.append("</div>");

        // Business plan section
        content.append("<div>");
        content.append("<h2>Kế hoạch kinh doanh mẫu</h2>");
        content.append("<div>");
        content.append("<ol>");
        content.append("<li>Tóm tắt điều hành</li>");
        content.append("<li>Mô tả công ty</li>");
        content.append("<li>Phân tích thị trường</li>");
        content.append("<li>Tổ chức và quản lý</li>");
        content.append("<li>Dòng sản phẩm hoặc dịch vụ</li>");
        content.append("<li>Chiến lược marketing và bán hàng</li>");
        content.append("<li>Dự báo tài chính</li>");
        content.append("</ol>");
        content.append("</div>");
        content.append("</div>");

        // References section
        content.append("<div>");
        content.append("<h2>Tài liệu tham khảo</h2>");
        content.append("<ul>");
        content.append("<li>\"Khởi nghiệp tinh gọn\" - Eric Ries</li>");
        content.append("<li>\"Tư duy như những nhà kinh doanh vĩ đại\" - Nguyễn Phi Vân</li>");
        content.append("<li>\"Quản trị marketing\" - Philip Kotler</li>");
        content.append("<li>\"Chiến lược đại dương xanh\" - W. Chan Kim và Renée Mauborgne</li>");
        content.append("</ul>");
        content.append("</div>");

        // Exercises section
        content.append("<div>");
        content.append("<h2>Bài tập thực hành</h2>");
        content.append("<ol>");
        content.append("<li>Xây dựng kế hoạch kinh doanh cho một sản phẩm hoặc dịch vụ mới</li>");
        content.append("<li>Thực hiện phân tích SWOT cho một doanh nghiệp thực tế</li>");
        content.append("<li>Thiết kế chiến lược marketing cho một thương hiệu</li>");
        content.append("</ol>");
        content.append("<p style=\"text-align: center;\"><strong>Chúc bạn thành công trong học tập và phát triển sự nghiệp!</strong></p>");
        content.append("</div>");

        // Add author and date info - useful for course versioning
        content.append("<div>");
        content.append("<p><em>Tác giả: lochuung</em></p>");
        content.append("<p><em>Cập nhật lần cuối: 2025-05-07</em></p>");
        content.append("</div>");

        return content.toString();
    }


    /**
     * Select appropriate content based on the category name
     */
    private String selectAppropriateContent(String courseName, String description, String categoryName) {
        // Check if this is a programming-related course
        if (categoryName.contains("lập trình") || categoryName.contains("Lập trình") ||
                categoryName.contains("ngôn ngữ") || categoryName.contains("IT") ||
                categoryName.contains("phần mềm") || categoryName.contains("web") ||
                categoryName.contains("CNTT") || categoryName.contains("phát triển")) {

            return createProgrammingReadingContent(courseName, categoryName);
        }

        // Check if this is a business/finance related course
        else if (categoryName.contains("kinh doanh") || categoryName.contains("Kinh doanh") ||
                categoryName.contains("tài chính") || categoryName.contains("Tài chính") ||
                categoryName.contains("quản lý") || categoryName.contains("Quản lý") ||
                categoryName.contains("tiếp thị") || categoryName.contains("Tiếp thị") ||
                categoryName.contains("marketing") || categoryName.contains("Marketing")) {

            return createBusinessReadingContent(courseName, categoryName);
        }

        // Default content for other categories
        return generateReadingContent(courseName, description, categoryName);
    }
}
