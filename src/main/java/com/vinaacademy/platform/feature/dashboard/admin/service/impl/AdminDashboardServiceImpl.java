package com.vinaacademy.platform.feature.dashboard.admin.service.impl;

import com.vinaacademy.platform.feature.course.entity.Course;
import com.vinaacademy.platform.feature.course.enums.CourseStatus;
import com.vinaacademy.platform.feature.course.repository.CourseRepository;
import com.vinaacademy.platform.feature.dashboard.admin.dto.*;
import com.vinaacademy.platform.feature.dashboard.admin.service.AdminDashboardService;
import com.vinaacademy.platform.feature.enrollment.repository.EnrollmentRepository;
import com.vinaacademy.platform.feature.order_payment.repository.OrderRepository;
import com.vinaacademy.platform.feature.revenue.service.PayoutService;
import com.vinaacademy.platform.feature.review.entity.CourseReview;
import com.vinaacademy.platform.feature.review.repository.CourseReviewRepository;
import com.vinaacademy.platform.feature.user.UserRepository;
import com.vinaacademy.platform.feature.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation của AdminDashboardService
 * Aggregate data từ các repositories để tạo dashboard statistics
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final OrderRepository orderRepository;
    private final CourseReviewRepository reviewRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PayoutService payoutService;

    private static final String ROLE_INSTRUCTOR = "instructor";
    private static final String ROLE_STUDENT = "student";
    private static final BigDecimal PLATFORM_FEE_RATE = new BigDecimal("0.20"); // 20%

    @Override
    public PlatformStatsDto getPlatformStats(String timeRange) {
        log.info("Getting platform stats for time range: {}", timeRange);

        LocalDateTime startDate = calculateStartDate(timeRange);
        LocalDateTime previousPeriodStart = calculatePreviousPeriodStart(timeRange, startDate);

        // Current period counts
        Long currentUsers = userRepository.count();
        Long currentCourses = courseRepository.count();
        Long currentInstructors = userRepository.countByRole(ROLE_INSTRUCTOR);
        BigDecimal currentRevenue = orderRepository.getTotalRevenueSince(
            LocalDateTime.now().minusYears(100) // All time
        );

        // Growth since start date
        Long newUsers = userRepository.countByCreatedDateAfter(startDate);
        Long newCourses = courseRepository.countByCreatedDateAfter(startDate);
        Long newInstructors = userRepository.countByRoleAndCreatedDateAfter(ROLE_INSTRUCTOR, startDate);
        BigDecimal periodRevenue = orderRepository.getTotalRevenueSince(startDate);

        // Previous period for comparison
        Long previousUsers = userRepository.countByCreatedDateBetween(previousPeriodStart, startDate);
        Long previousCourses = courseRepository.countByCreatedDateBetween(previousPeriodStart, startDate);
        Long previousInstructors = userRepository.countByRoleAndCreatedDateBetween(
            ROLE_INSTRUCTOR, previousPeriodStart, startDate
        );
        BigDecimal previousRevenue = orderRepository.getTotalRevenueBetween(previousPeriodStart, startDate);

        // Calculate percentage changes
        Double userChange = calculatePercentageChange(previousUsers, newUsers);
        Double courseChange = calculatePercentageChange(previousCourses, newCourses);
        Double instructorChange = calculatePercentageChange(previousInstructors, newInstructors);
        Double revenueChange = calculatePercentageChange(previousRevenue, periodRevenue);

        return PlatformStatsDto.builder()
            .totalUsers(currentUsers)
            .userChange(userChange)
            .totalCourses(currentCourses)
            .courseChange(courseChange)
            .totalInstructors(currentInstructors)
            .instructorChange(instructorChange)
            .totalRevenue(currentRevenue)
            .revenueChange(revenueChange)
            .timeRange(timeRange)
            .build();
    }

    @Override
    public RevenueOverviewDto getRevenueOverview() {
        log.info("Getting revenue overview");

        LocalDateTime startDate = LocalDateTime.now().minusMonths(12);

        // Get monthly revenue data
        List<Object[]> monthlyData = orderRepository.getMonthlyRevenue(startDate);
        List<MonthlyRevenueDto> monthlyRevenue = mapToMonthlyRevenueDto(monthlyData);

        // Get category distribution
        List<Object[]> categoryData = orderRepository.getRevenueByCategory();
        List<RevenueDistributionDto> distribution = mapToDistributionDto(categoryData);

        // Calculate totals
        BigDecimal yearlyRevenue = orderRepository.getTotalRevenueSince(startDate);
        if (yearlyRevenue == null) yearlyRevenue = BigDecimal.ZERO;
        
        BigDecimal platformFee = yearlyRevenue.multiply(PLATFORM_FEE_RATE)
            .setScale(0, RoundingMode.HALF_UP);
        BigDecimal instructorEarnings = yearlyRevenue.subtract(platformFee);

        // Calculate average order value
        BigDecimal averageOrderValue = orderRepository.getAverageOrderValue(startDate);
        if (averageOrderValue == null) averageOrderValue = BigDecimal.ZERO;

        return RevenueOverviewDto.builder()
            .monthlyRevenue(monthlyRevenue)
            .distribution(distribution)
            .yearlyRevenue(yearlyRevenue)
            .platformFee(platformFee)
            .instructorEarnings(instructorEarnings)
            .averageOrderValue(averageOrderValue)
            .build();
    }

    @Override
    public ActiveUsersDto getActiveUsers() {
        log.info("Getting active users statistics");

        LocalDateTime startDate = LocalDateTime.now().minusMonths(12);

        // Monthly active users data
        List<Object[]> monthlyData = userRepository.getMonthlyUserStats(startDate);
        List<MonthlyUsersDto> monthlyUsers = mapToMonthlyUsersDto(monthlyData);

        // Total counts
        Long totalUsers = userRepository.count();
        Long studentCount = userRepository.countByRole(ROLE_STUDENT);
        Long instructorCount = userRepository.countByRole(ROLE_INSTRUCTOR);

        // Calculate percentages
        Double studentPercentage = calculatePercentage(studentCount, totalUsers);
        Double instructorPercentage = calculatePercentage(instructorCount, totalUsers);

        // Calculate user growth (last month vs previous month)
        LocalDateTime lastMonthStart = LocalDateTime.now().minusMonths(1);
        LocalDateTime twoMonthsAgo = LocalDateTime.now().minusMonths(2);
        Long lastMonthUsers = userRepository.countByCreatedDateBetween(lastMonthStart, LocalDateTime.now());
        Long previousMonthUsers = userRepository.countByCreatedDateBetween(twoMonthsAgo, lastMonthStart);
        Double userGrowth = calculatePercentageChange(previousMonthUsers, lastMonthUsers);

        // Retention rate (users active in last 30 days / total users)
        Long activeLastMonth = userRepository.countActiveUsersSince(LocalDateTime.now().minusDays(30));
        Double retentionRate = calculatePercentage(activeLastMonth, totalUsers);

        // Device stats (mock for now - would need additional tracking)
        DevicePlatformDto deviceStats = DevicePlatformDto.builder()
            .mobilePercentage(62.0)
            .desktopPercentage(32.0)
            .tabletPercentage(6.0)
            .build();

        return ActiveUsersDto.builder()
            .monthlyData(monthlyUsers)
            .totalUsers(totalUsers)
            .userGrowth(userGrowth)
            .studentCount(studentCount)
            .studentPercentage(studentPercentage)
            .instructorCount(instructorCount)
            .instructorPercentage(instructorPercentage)
            .retentionRate(retentionRate)
            .deviceStats(deviceStats)
            .build();
    }

    @Override
    public RecentActivitiesDto getRecentActivities() {
        log.info("Getting recent activities");

        // Recent courses (last 5)
        Page<Course> coursesPage = courseRepository.findRecentCourses(PageRequest.of(0, 5));
        List<RecentCourseDto> recentCourses = coursesPage.getContent().stream()
            .map(this::mapToRecentCourseDto)
            .collect(Collectors.toList());

        // Recent instructors (last 5)
        Page<User> instructorsPage = userRepository.findRecentInstructors(PageRequest.of(0, 5));
        List<RecentInstructorDto> recentInstructors = instructorsPage.getContent().stream()
            .map(this::mapToRecentInstructorDto)
            .collect(Collectors.toList());

        // Recent reviews (last 5)
        Page<CourseReview> reviewsPage = reviewRepository.findRecentReviews(PageRequest.of(0, 5));
        List<RecentReviewDto> recentReviews = reviewsPage.getContent().stream()
            .map(this::mapToRecentReviewDto)
            .collect(Collectors.toList());

        return RecentActivitiesDto.builder()
            .recentCourses(recentCourses)
            .recentInstructors(recentInstructors)
            .recentReviews(recentReviews)
            .build();
    }

    @Override
    public QuickActionsDto getQuickActions() {
        log.info("Getting quick actions counts");

        Long pendingCourses = courseRepository.countByStatus(CourseStatus.PENDING);
        Long pendingWithdrawals = payoutService.countPendingPayouts();
        
        // These would need additional implementation if you have these features
        Long reportedViolations = 0L; // TODO: Implement if you have report system
        Long pendingSupports = 0L;    // TODO: Implement if you have support system

        return QuickActionsDto.builder()
            .pendingCourses(pendingCourses)
            .pendingWithdrawals(pendingWithdrawals)
            .reportedViolations(reportedViolations)
            .pendingSupports(pendingSupports)
            .build();
    }

    // ==================== Helper Methods ====================

    /**
     * Calculate start date based on time range
     */
    private LocalDateTime calculateStartDate(String timeRange) {
        return switch (timeRange.toLowerCase()) {
            case "week" -> LocalDateTime.now().minusWeeks(1);
            case "month" -> LocalDateTime.now().minusMonths(1);
            case "year" -> LocalDateTime.now().minusYears(1);
            default -> LocalDateTime.now().minusMonths(1);
        };
    }

    /**
     * Calculate start date for previous period (for comparison)
     */
    private LocalDateTime calculatePreviousPeriodStart(String timeRange, LocalDateTime currentStart) {
        return switch (timeRange.toLowerCase()) {
            case "week" -> currentStart.minusWeeks(1);
            case "month" -> currentStart.minusMonths(1);
            case "year" -> currentStart.minusYears(1);
            default -> currentStart.minusMonths(1);
        };
    }

    /**
     * Calculate percentage change between two numbers
     */
    private Double calculatePercentageChange(Number previous, Number current) {
        if (previous == null || current == null) return 0.0;
        
        double prev = previous.doubleValue();
        double curr = current.doubleValue();
        
        if (prev == 0) {
            return curr > 0 ? 100.0 : 0.0;
        }
        
        return ((curr - prev) / prev) * 100;
    }

    /**
     * Calculate percentage (part/total * 100)
     */
    private Double calculatePercentage(Long part, Long total) {
        if (part == null || total == null || total == 0) return 0.0;
        return (part.doubleValue() / total.doubleValue()) * 100;
    }

    /**
     * Map monthly revenue query results to DTOs
     * Query returns: [year-month, revenue, orderCount]
     */
    private List<MonthlyRevenueDto> mapToMonthlyRevenueDto(List<Object[]> queryResults) {
        if (queryResults == null || queryResults.isEmpty()) {
            return generateEmptyMonthlyRevenue();
        }

        Map<String, MonthlyRevenueDto> dataMap = new HashMap<>();
        
        for (Object[] row : queryResults) {
            String yearMonth = (String) row[0]; // "2024-12"
            BigDecimal revenue = (BigDecimal) row[1];
            Long orderCount = (Long) row[2];

            String monthLabel = formatMonthLabel(yearMonth);
            
            dataMap.put(monthLabel, MonthlyRevenueDto.builder()
                .month(monthLabel)
                .revenue(revenue)
                .courses(orderCount.intValue())
                .build());
        }

        // Fill in missing months with zeros
        return fillMissingMonths(dataMap);
    }

    /**
     * Map revenue distribution query results to DTOs
     * Query returns: [category_name, total_revenue]
     */
    private List<RevenueDistributionDto> mapToDistributionDto(List<Object[]> queryResults) {
        if (queryResults == null || queryResults.isEmpty()) {
            return new ArrayList<>();
        }

        // Calculate total revenue first
        BigDecimal totalRevenue = queryResults.stream()
            .map(row -> (BigDecimal) row[1])
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalRevenue.compareTo(BigDecimal.ZERO) == 0) {
            return new ArrayList<>();
        }

        return queryResults.stream()
            .map(row -> {
                String categoryName = (String) row[0];
                BigDecimal amount = (BigDecimal) row[1];
                Double percentage = amount.multiply(new BigDecimal("100"))
                    .divide(totalRevenue, 2, RoundingMode.HALF_UP)
                    .doubleValue();

                return RevenueDistributionDto.builder()
                    .categoryName(categoryName)
                    .percentage(percentage)
                    .amount(amount)
                    .build();
            })
            .collect(Collectors.toList());
    }

    /**
     * Map monthly users query results to DTOs
     * Query returns: [year-month, count]
     */
    private List<MonthlyUsersDto> mapToMonthlyUsersDto(List<Object[]> queryResults) {
        if (queryResults == null || queryResults.isEmpty()) {
            return generateEmptyMonthlyUsers();
        }

        Map<String, MonthlyUsersDto> dataMap = new HashMap<>();
        
        for (Object[] row : queryResults) {
            String yearMonth = (String) row[0]; // "2024-12"
            Long count = (Long) row[1];

            String monthLabel = formatMonthLabel(yearMonth);
            
            dataMap.put(monthLabel, MonthlyUsersDto.builder()
                .month(monthLabel)
                .activeUsers(count)
                .newUsers(count) // Same as active for now
                .build());
        }

        return fillMissingMonthsForUsers(dataMap);
    }

    /**
     * Map Course entity to RecentCourseDto
     */
    private RecentCourseDto mapToRecentCourseDto(Course course) {
        // Get enrollment count
        Long enrollmentCount = enrollmentRepository.countByCourseId(course.getId());
        
        return RecentCourseDto.builder()
            .id(course.getId().toString())
            .title(course.getName())
            .thumbnail(course.getImage())
            .enrollmentCount(enrollmentCount.intValue())
            .status(course.getStatus().name())
            .createdAt(course.getCreatedDate())
            .build();
    }

    /**
     * Map User entity to RecentInstructorDto
     */
    private RecentInstructorDto mapToRecentInstructorDto(User user) {
        String initials = generateInitials(user.getFullName());
        String expertise = determineExpertise(user);
        
        return RecentInstructorDto.builder()
            .userId(user.getId().toString())
            .name(user.getFullName())
            .initials(initials)
            .expertise(expertise)
            .joinedAt(user.getCreatedDate())
            .build();
    }

    /**
     * Map CourseReview entity to RecentReviewDto
     */
    private RecentReviewDto mapToRecentReviewDto(CourseReview review) {
        String studentName = review.getUser() != null ? review.getUser().getFullName() : "Unknown";
        String studentInitials = generateInitials(studentName);
        String courseTitle = review.getCourse() != null ? review.getCourse().getName() : "Unknown Course";
        
        return RecentReviewDto.builder()
            .studentName(studentName)
            .studentInitials(studentInitials)
            .rating(review.getRating())
            .comment(review.getReview())
            .courseTitle(courseTitle)
            .createdAt(review.getCreatedDate())
            .build();
    }

    /**
     * Generate initials from full name
     */
    private String generateInitials(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "??";
        }

        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }

        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    /**
     * Determine instructor's expertise based on courses taught
     */
    private String determineExpertise(User instructor) {
        // This is a simplified version
        // In reality, you might want to query the most common category of their courses
        Long courseCount = courseRepository.countCoursesByInstructorId(instructor.getId());
        
        if (courseCount > 10) return "Senior Instructor";
        if (courseCount > 5) return "Experienced Instructor";
        return "New Instructor";
    }

    /**
     * Format year-month string to month label
     * "2024-12" -> "T12"
     */
    private String formatMonthLabel(String yearMonth) {
        if (yearMonth == null || !yearMonth.contains("-")) {
            return "T1";
        }
        
        String[] parts = yearMonth.split("-");
        if (parts.length >= 2) {
            String month = parts[1].replaceFirst("^0+", ""); // Remove leading zeros
            return "T" + month;
        }
        
        return "T1";
    }

    /**
     * Generate empty monthly revenue data for 12 months
     */
    private List<MonthlyRevenueDto> generateEmptyMonthlyRevenue() {
        List<MonthlyRevenueDto> result = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            result.add(MonthlyRevenueDto.builder()
                .month("T" + i)
                .revenue(BigDecimal.ZERO)
                .courses(0)
                .build());
        }
        return result;
    }

    /**
     * Generate empty monthly users data for 12 months
     */
    private List<MonthlyUsersDto> generateEmptyMonthlyUsers() {
        List<MonthlyUsersDto> result = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            result.add(MonthlyUsersDto.builder()
                .month("T" + i)
                .activeUsers(0L)
                .newUsers(0L)
                .build());
        }
        return result;
    }

    /**
     * Fill missing months in revenue data
     */
    private List<MonthlyRevenueDto> fillMissingMonths(Map<String, MonthlyRevenueDto> dataMap) {
        List<MonthlyRevenueDto> result = new ArrayList<>();
        
        for (int i = 1; i <= 12; i++) {
            String monthLabel = "T" + i;
            result.add(dataMap.getOrDefault(monthLabel, 
                MonthlyRevenueDto.builder()
                    .month(monthLabel)
                    .revenue(BigDecimal.ZERO)
                    .courses(0)
                    .build()
            ));
        }
        
        return result;
    }

    /**
     * Fill missing months in users data
     */
    private List<MonthlyUsersDto> fillMissingMonthsForUsers(Map<String, MonthlyUsersDto> dataMap) {
        List<MonthlyUsersDto> result = new ArrayList<>();
        
        for (int i = 1; i <= 12; i++) {
            String monthLabel = "T" + i;
            result.add(dataMap.getOrDefault(monthLabel,
                MonthlyUsersDto.builder()
                    .month(monthLabel)
                    .activeUsers(0L)
                    .newUsers(0L)
                    .build()
            ));
        }
        
        return result;
    }
}
