package com.vinaacademy.platform.feature.instructor.service;

import com.vinaacademy.platform.feature.course.entity.Course;
import com.vinaacademy.platform.feature.course.repository.CourseRepository;
import com.vinaacademy.platform.feature.enrollment.Enrollment;
import com.vinaacademy.platform.feature.enrollment.enums.ProgressStatus;
import com.vinaacademy.platform.feature.enrollment.repository.EnrollmentRepository;
import com.vinaacademy.platform.feature.instructor.dto.CourseOverviewDto;
import com.vinaacademy.platform.feature.instructor.dto.DashboardStatisticsDto;
import com.vinaacademy.platform.feature.instructor.dto.RecentActivitiesDto;
import com.vinaacademy.platform.feature.instructor.dto.RevenueChartDto;
import com.vinaacademy.platform.feature.instructor.dto.StudentsChartDto;
import com.vinaacademy.platform.feature.revenue.entity.RevenueRecord;
import com.vinaacademy.platform.feature.revenue.repository.RevenueRecordRepository;
import com.vinaacademy.platform.feature.review.entity.CourseReview;
import com.vinaacademy.platform.feature.review.repository.CourseReviewRepository;
import com.vinaacademy.platform.feature.user.entity.User;
import com.vinaacademy.platform.feature.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation của InstructorDashboardService
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InstructorDashboardServiceImpl implements InstructorDashboardService {

    private final UserService userService;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final RevenueRecordRepository revenueRecordRepository;
    private final CourseReviewRepository courseReviewRepository;

    @Override
    public DashboardStatisticsDto getDashboardStatistics(String timeRange) {
        UUID instructorId = userService.getCurrentUser().getId();
        log.info("Getting dashboard statistics for instructor: {} with timeRange: {}", instructorId, timeRange);

        // Xác định khoảng thời gian
        TimeRange range = getTimeRange(timeRange);

        // Tính thống kê doanh thu
        DashboardStatisticsDto.RevenueStats revenueStats = calculateRevenueStats(instructorId, range);

        // Tính thống kê học viên mới
        DashboardStatisticsDto.StudentStats studentStats = calculateStudentStats(instructorId, range);

        // Tính thống kê đánh giá trung bình
        DashboardStatisticsDto.RatingStats ratingStats = calculateRatingStats(instructorId);

        // Đếm tổng số khóa học
        Page<Course> courses = courseRepository.findByInstructorId(instructorId, Pageable.unpaged());
        int totalCourses = (int) courses.getTotalElements();

        // Tính tỷ lệ hoàn thành trung bình
        Double completionRate = calculateCompletionRate(instructorId);

        return DashboardStatisticsDto.builder()
                .revenue(revenueStats)
                .newStudents(studentStats)
                .averageRating(ratingStats)
                .totalCourses(totalCourses)
                .completionRate(completionRate)
                .build();
    }

    /**
     * Tính thống kê doanh thu
     */
    private DashboardStatisticsDto.RevenueStats calculateRevenueStats(UUID instructorId, TimeRange range) {
        // Doanh thu kỳ hiện tại
        BigDecimal currentRevenue = revenueRecordRepository.getTotalEarningsByInstructorAndDateRange(
                instructorId,
                range.currentStart,
                range.currentEnd
        );

        // Doanh thu kỳ trước
        BigDecimal previousRevenue = revenueRecordRepository.getTotalEarningsByInstructorAndDateRange(
                instructorId,
                range.previousStart,
                range.previousEnd
        );

        // Tính % thay đổi
        Double changePercentage = calculatePercentageChange(
                previousRevenue != null ? previousRevenue : BigDecimal.ZERO,
                currentRevenue != null ? currentRevenue : BigDecimal.ZERO
        );

        return DashboardStatisticsDto.RevenueStats.builder()
                .current(currentRevenue != null ? currentRevenue : BigDecimal.ZERO)
                .change(changePercentage)
                .isIncrease(changePercentage >= 0)
                .build();
    }

    /**
     * Tính thống kê học viên mới
     */
    private DashboardStatisticsDto.StudentStats calculateStudentStats(UUID instructorId, TimeRange range) {
        // Lấy danh sách khóa học của giảng viên
        Page<Course> courses = courseRepository.findByInstructorId(instructorId, Pageable.unpaged());
        List<UUID> courseIds = courses.getContent().stream()
                .map(Course::getId)
                .toList();

        // Đếm học viên mới trong kỳ hiện tại
        Long currentStudents = enrollmentRepository.countByCourseIdsAndDateRange(
                courseIds,
                range.currentStart,
                range.currentEnd
        );

        // Đếm học viên mới trong kỳ trước
        Long previousStudents = enrollmentRepository.countByCourseIdsAndDateRange(
                courseIds,
                range.previousStart,
                range.previousEnd
        );

        // Tính % thay đổi
        Double changePercentage = calculatePercentageChange(
                previousStudents != null ? previousStudents : 0L,
                currentStudents != null ? currentStudents : 0L
        );

        return DashboardStatisticsDto.StudentStats.builder()
                .current(currentStudents != null ? currentStudents : 0L)
                .change(changePercentage)
                .isIncrease(changePercentage >= 0)
                .build();
    }

    /**
     * Tính thống kê đánh giá trung bình
     */
    private DashboardStatisticsDto.RatingStats calculateRatingStats(UUID instructorId) {
        // Lấy danh sách khóa học của giảng viên
        Page<Course> courses = courseRepository.findByInstructorId(instructorId, Pageable.unpaged());

        // Tính rating trung bình và tổng reviews
        double totalRating = 0.0;
        long totalReviews = 0L;
        int courseCount = 0;

        for (Course course : courses.getContent()) {
            if (course.getRating() > 0) {
                totalRating += course.getRating();
                courseCount++;
            }
            totalReviews += course.getTotalRating();
        }

        double averageRating = courseCount > 0 ? totalRating / courseCount : 0.0;

        return DashboardStatisticsDto.RatingStats.builder()
                .current(Math.round(averageRating * 10.0) / 10.0) // Round to 1 decimal
                .totalReviews(totalReviews)
                .build();
    }

    /**
     * Tính tỷ lệ hoàn thành trung bình
     */
    private Double calculateCompletionRate(UUID instructorId) {
        // Lấy danh sách khóa học của giảng viên
        Page<Course> courses = courseRepository.findByInstructorId(instructorId, Pageable.unpaged());
        List<UUID> courseIds = courses.getContent().stream()
                .map(Course::getId)
                .toList();

        if (courseIds.isEmpty()) {
            return 0.0;
        }

        // Đếm tổng số enrollments
        long totalEnrollments = enrollmentRepository.countByCourseIds(courseIds);

        if (totalEnrollments == 0) {
            return 0.0;
        }

        // Đếm số enrollments đã hoàn thành
        long completedEnrollments = enrollmentRepository.countByCourseIdsAndStatus(
                courseIds,
                ProgressStatus.COMPLETED
        );

        // Tính tỷ lệ %
        double rate = (completedEnrollments * 100.0) / totalEnrollments;
        return Math.round(rate * 10.0) / 10.0; // Round to 1 decimal
    }

    /**
     * Tính % thay đổi giữa 2 giá trị BigDecimal
     */
    private Double calculatePercentageChange(BigDecimal previous, BigDecimal current) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current != null && current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }

        BigDecimal change = current.subtract(previous);
        BigDecimal percentage = change
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        return percentage.setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Tính % thay đổi giữa 2 giá trị Long
     */
    private Double calculatePercentageChange(Long previous, Long current) {
        if (previous == null || previous == 0) {
            return current != null && current > 0 ? 100.0 : 0.0;
        }

        double change = current - previous;
        double percentage = (change / previous) * 100;

        return Math.round(percentage * 10.0) / 10.0;
    }

    /**
     * Xác định khoảng thời gian dựa trên timeRange
     */
    private TimeRange getTimeRange(String timeRange) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime currentStart, currentEnd, previousStart, previousEnd;

        switch (timeRange.toUpperCase()) {
            case "WEEK":
                currentEnd = now;
                currentStart = now.minusWeeks(1);
                previousEnd = currentStart;
                previousStart = previousEnd.minusWeeks(1);
                break;

            case "YEAR":
                currentEnd = now;
                currentStart = now.minusYears(1);
                previousEnd = currentStart;
                previousStart = previousEnd.minusYears(1);
                break;

            case "MONTH":
            default:
                currentEnd = now;
                currentStart = now.minusMonths(1);
                previousEnd = currentStart;
                previousStart = previousEnd.minusMonths(1);
                break;
        }

        return new TimeRange(currentStart, currentEnd, previousStart, previousEnd);
    }

    /**
     * Inner class để lưu thông tin time range
     */
    private static class TimeRange {
        final LocalDateTime currentStart;
        final LocalDateTime currentEnd;
        final LocalDateTime previousStart;
        final LocalDateTime previousEnd;

        TimeRange(LocalDateTime currentStart, LocalDateTime currentEnd,
                  LocalDateTime previousStart, LocalDateTime previousEnd) {
            this.currentStart = currentStart;
            this.currentEnd = currentEnd;
            this.previousStart = previousStart;
            this.previousEnd = previousEnd;
        }
    }

    @Override
    public CourseOverviewDto getCourseOverview(String sortBy) {
        UUID instructorId = userService.getCurrentUser().getId();
        log.info("Getting course overview for instructor: {} with sortBy: {}", instructorId, sortBy);

        // Lấy tất cả khóa học của giảng viên
        Page<Course> coursesPage = courseRepository.findByInstructorId(instructorId, Pageable.unpaged());
        List<Course> courses = coursesPage.getContent();

        // Tính summary
        CourseOverviewDto.Summary summary = calculateCourseSummary(courses);

        // Tạo danh sách course details
        List<CourseOverviewDto.CourseDetail> courseDetails = courses.stream()
                .map(course -> buildCourseDetail(course))
                .collect(Collectors.toList());

        // Sắp xếp theo sortBy
        sortCourseDetails(courseDetails, sortBy);

        return CourseOverviewDto.builder()
                .summary(summary)
                .courses(courseDetails)
                .build();
    }

    /**
     * Tính tổng quan summary
     */
    private CourseOverviewDto.Summary calculateCourseSummary(List<Course> courses) {
        int totalCourses = courses.size();
        long totalStudents = courses.stream()
                .mapToLong(Course::getTotalStudent)
                .sum();

        // Tính tổng doanh thu từ tất cả các courses
        List<UUID> courseIds = courses.stream()
                .map(Course::getId)
                .collect(Collectors.toList());

        BigDecimal totalRevenue = courseIds.isEmpty() 
                ? BigDecimal.ZERO 
                : revenueRecordRepository.getTotalEarningsByCourseIds(courseIds);

        // Tính average completion rate
        double averageCompletionRate = 0.0;
        if (!courseIds.isEmpty()) {
            long totalEnrollments = enrollmentRepository.countByCourseIds(courseIds);
            if (totalEnrollments > 0) {
                long completedEnrollments = enrollmentRepository.countByCourseIdsAndStatus(
                        courseIds, ProgressStatus.COMPLETED);
                averageCompletionRate = (completedEnrollments * 100.0) / totalEnrollments;
                averageCompletionRate = Math.round(averageCompletionRate * 10.0) / 10.0;
            }
        }

        return CourseOverviewDto.Summary.builder()
                .totalCourses(totalCourses)
                .averageCompletionRate(averageCompletionRate)
                .totalStudents(totalStudents)
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .build();
    }

    /**
     * Tạo course detail từ Course entity
     */
    private CourseOverviewDto.CourseDetail buildCourseDetail(Course course) {
        // Tính doanh thu cho course này
        BigDecimal courseRevenue = revenueRecordRepository.getTotalEarningsByCourseId(course.getId());

        // Tính completion rate cho course này
        long totalEnrollments = enrollmentRepository.countByCourseId(course.getId());
        double completionRate = 0.0;
        if (totalEnrollments > 0) {
            long completedEnrollments = enrollmentRepository.countByCourseIdAndStatus(
                    course.getId(), ProgressStatus.COMPLETED);
            completionRate = (completedEnrollments * 100.0) / totalEnrollments;
            completionRate = Math.round(completionRate * 10.0) / 10.0;
        }

        return CourseOverviewDto.CourseDetail.builder()
                .id(course.getId())
                .name(course.getName())
                .students(course.getTotalStudent())
                .rating(course.getRating())
                .totalReviews(course.getTotalRating())
                .revenue(courseRevenue != null ? courseRevenue : BigDecimal.ZERO)
                .price(course.getPrice() != null ? course.getPrice() : BigDecimal.ZERO)
                .completionRate(completionRate)
                .lastUpdated(course.getUpdatedDate() != null ? course.getUpdatedDate() : course.getCreatedDate())
                .status(course.getStatus() != null ? course.getStatus().name() : "DRAFT")
                .build();
    }

    /**
     * Sắp xếp course details theo sortBy
     */
    private void sortCourseDetails(List<CourseOverviewDto.CourseDetail> courseDetails, String sortBy) {
        if (sortBy == null) {
            sortBy = "POPULAR";
        }

        switch (sortBy.toUpperCase()) {
            case "RECENT":
                courseDetails.sort(Comparator.comparing(CourseOverviewDto.CourseDetail::getLastUpdated).reversed());
                break;
            case "REVENUE":
                courseDetails.sort(Comparator.comparing(CourseOverviewDto.CourseDetail::getRevenue).reversed());
                break;
            case "POPULAR":
            default:
                courseDetails.sort(Comparator.comparing(CourseOverviewDto.CourseDetail::getStudents).reversed());
                break;
        }
    }

    @Override
    public RecentActivitiesDto getRecentActivities(Integer limit) {
        UUID instructorId = userService.getCurrentUser().getId();
        log.info("Getting recent activities for instructor: {} with limit: {}", instructorId, limit);

        // Mặc định lấy 5 items nếu không truyền limit
        if (limit == null || limit <= 0) {
            limit = 5;
        }

        // Lấy danh sách courseIds của giảng viên
        Page<Course> coursesPage = courseRepository.findByInstructorId(instructorId, Pageable.unpaged());
        List<UUID> courseIds = coursesPage.getContent().stream()
                .map(Course::getId)
                .collect(Collectors.toList());

        if (courseIds.isEmpty()) {
            return RecentActivitiesDto.builder()
                    .recentEnrollments(new ArrayList<>())
                    .recentReviews(new ArrayList<>())
                    .build();
        }

        // Lấy enrollments gần đây
        Pageable enrollmentPageable = PageRequest.of(0, limit);
        Page<Enrollment> enrollmentsPage = enrollmentRepository.findRecentEnrollmentsByCourseIds(
                courseIds, enrollmentPageable);

        List<RecentActivitiesDto.EnrollmentActivity> enrollmentActivities = enrollmentsPage.getContent().stream()
                .map(this::buildEnrollmentActivity)
                .collect(Collectors.toList());

        // Lấy reviews gần đây
        Pageable reviewPageable = PageRequest.of(0, limit);
        Page<CourseReview> reviewsPage = courseReviewRepository.findRecentReviewsByCourseIds(
                courseIds, reviewPageable);

        List<RecentActivitiesDto.ReviewActivity> reviewActivities = reviewsPage.getContent().stream()
                .map(this::buildReviewActivity)
                .collect(Collectors.toList());

        return RecentActivitiesDto.builder()
                .recentEnrollments(enrollmentActivities)
                .recentReviews(reviewActivities)
                .build();
    }

    /**
     * Tạo EnrollmentActivity từ Enrollment entity
     */
    private RecentActivitiesDto.EnrollmentActivity buildEnrollmentActivity(Enrollment enrollment) {
        User student = enrollment.getUser();
        Course course = enrollment.getCourse();

        return RecentActivitiesDto.EnrollmentActivity.builder()
                .id(enrollment.getId())
                .studentName(student.getFullName() != null ? student.getFullName() : student.getEmail())
                .studentAvatar(student.getAvatarUrl())
                .courseName(course.getName())
                .enrolledAt(enrollment.getStartAt())
                .build();
    }

    /**
     * Tạo ReviewActivity từ CourseReview entity
     */
    private RecentActivitiesDto.ReviewActivity buildReviewActivity(CourseReview review) {
        User student = review.getUser();
        Course course = review.getCourse();

        return RecentActivitiesDto.ReviewActivity.builder()
                .id(review.getId())
                .studentName(student.getFullName() != null ? student.getFullName() : student.getEmail())
                .studentAvatar(student.getAvatarUrl())
                .courseName(course.getName())
                .rating(review.getRating())
                .comment(review.getReview())
                .reviewedAt(review.getCreatedDate())
                .build();
    }

    @Override
    public RevenueChartDto getRevenueChart(String period) {
        UUID instructorId = userService.getCurrentUser().getId();
        log.info("Getting revenue chart for instructor: {} with period: {}", instructorId, period);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate;
        List<RevenueChartDto.RevenueDataPoint> dataPoints = new ArrayList<>();

        if ("WEEK".equalsIgnoreCase(period)) {
            // 7 ngày gần nhất
            startDate = now.minusDays(6).withHour(0).withMinute(0).withSecond(0);
            dataPoints = getRevenueByDays(instructorId, startDate, now, 7);
        } else if ("YEAR".equalsIgnoreCase(period)) {
            // 12 tháng của năm hiện tại
            startDate = LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0);
            LocalDateTime endDate = LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59);
            dataPoints = getRevenueByMonths(instructorId, startDate, endDate, 12, true);
        } else {
            // MONTH: 12 tháng gần nhất
            startDate = now.minusMonths(11).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            dataPoints = getRevenueByMonths(instructorId, startDate, now, 12, false);
        }

        return RevenueChartDto.builder()
                .data(dataPoints)
                .build();
    }

    /**
     * Lấy doanh thu theo từng ngày
     */
    private List<RevenueChartDto.RevenueDataPoint> getRevenueByDays(
            UUID instructorId, LocalDateTime startDate, LocalDateTime endDate, int days) {
        
        List<RevenueRecord> records = revenueRecordRepository.findByInstructorIdAndDateRange(
                instructorId, startDate, endDate);

        // Tạo map để lưu doanh thu theo ngày
        Map<String, BigDecimal> revenueByDay = new LinkedHashMap<>();
        
        // Khởi tạo tất cả các ngày với giá trị 0
        for (int i = 0; i < days; i++) {
            LocalDateTime date = startDate.plusDays(i);
            String dayKey = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            revenueByDay.put(dayKey, BigDecimal.ZERO);
        }

        // Tổng hợp doanh thu theo ngày
        for (RevenueRecord record : records) {
            String dayKey = record.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            if (revenueByDay.containsKey(dayKey)) {
                BigDecimal currentRevenue = revenueByDay.get(dayKey);
                revenueByDay.put(dayKey, currentRevenue.add(record.getInstructorEarning()));
            }
        }

        // Chuyển đổi sang list data points
        List<RevenueChartDto.RevenueDataPoint> dataPoints = new ArrayList<>();
        int dayIndex = 1;
        for (Map.Entry<String, BigDecimal> entry : revenueByDay.entrySet()) {
            dataPoints.add(RevenueChartDto.RevenueDataPoint.builder()
                    .name("Ngày " + dayIndex)
                    .revenue(entry.getValue())
                    .build());
            dayIndex++;
        }

        return dataPoints;
    }

    /**
     * Lấy doanh thu theo từng tháng
     */
    private List<RevenueChartDto.RevenueDataPoint> getRevenueByMonths(
            UUID instructorId, LocalDateTime startDate, LocalDateTime endDate, 
            int months, boolean useMonthNumber) {
        
        List<RevenueRecord> records = revenueRecordRepository.findByInstructorIdAndDateRange(
                instructorId, startDate, endDate);

        // Tạo map để lưu doanh thu theo tháng
        Map<String, BigDecimal> revenueByMonth = new LinkedHashMap<>();
        
        // Khởi tạo tất cả các tháng với giá trị 0
        for (int i = 0; i < months; i++) {
            LocalDateTime monthDate = startDate.plusMonths(i);
            String monthKey = monthDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            revenueByMonth.put(monthKey, BigDecimal.ZERO);
        }

        // Tổng hợp doanh thu theo tháng
        for (RevenueRecord record : records) {
            String monthKey = record.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            if (revenueByMonth.containsKey(monthKey)) {
                BigDecimal currentRevenue = revenueByMonth.get(monthKey);
                revenueByMonth.put(monthKey, currentRevenue.add(record.getInstructorEarning()));
            }
        }

        // Chuyển đổi sang list data points
        List<RevenueChartDto.RevenueDataPoint> dataPoints = new ArrayList<>();
        int monthIndex = 1;
        for (Map.Entry<String, BigDecimal> entry : revenueByMonth.entrySet()) {
            String name;
            if (useMonthNumber) {
                // Dùng cho YEAR: T1, T2, ... T12
                name = "T" + monthIndex;
            } else {
                // Dùng cho MONTH: T1, T2, ... T12 (12 tháng gần nhất)
                name = "T" + monthIndex;
            }
            
            dataPoints.add(RevenueChartDto.RevenueDataPoint.builder()
                    .name(name)
                    .revenue(entry.getValue())
                    .build());
            monthIndex++;
        }

        return dataPoints;
    }

    @Override
    public StudentsChartDto getStudentsChart(String period) {
        UUID instructorId = userService.getCurrentUser().getId();
        log.info("Getting students chart for instructor: {} with period: {}", instructorId, period);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate;
        List<StudentsChartDto.StudentDataPoint> dataPoints = new ArrayList<>();

        if ("WEEK".equalsIgnoreCase(period)) {
            // 7 ngày gần nhất
            startDate = now.minusDays(6).withHour(0).withMinute(0).withSecond(0);
            dataPoints = getStudentsByDays(instructorId, startDate, now, 7);
        } else if ("YEAR".equalsIgnoreCase(period)) {
            // 12 tháng của năm hiện tại
            startDate = LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0);
            LocalDateTime endDate = LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59);
            dataPoints = getStudentsByMonths(instructorId, startDate, endDate, 12, true);
        } else {
            // MONTH: 12 tháng gần nhất
            startDate = now.minusMonths(11).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            dataPoints = getStudentsByMonths(instructorId, startDate, now, 12, false);
        }

        return StudentsChartDto.builder()
                .data(dataPoints)
                .build();
    }

    /**
     * Lấy số lượng học viên theo từng ngày
     */
    private List<StudentsChartDto.StudentDataPoint> getStudentsByDays(
            UUID instructorId, LocalDateTime startDate, LocalDateTime endDate, int days) {
        
        // Lấy tất cả courses của instructor
        List<Course> courses = courseRepository.findByInstructorId(instructorId, Pageable.unpaged()).getContent();
        List<UUID> courseIds = courses.stream().map(Course::getId).collect(Collectors.toList());

        // Lấy tất cả enrollments trong khoảng thời gian
        List<Enrollment> enrollments = enrollmentRepository.findByStartAtBetween(startDate, endDate);
        
        // Lọc chỉ lấy enrollments của courses thuộc instructor
        enrollments = enrollments.stream()
                .filter(e -> courseIds.contains(e.getCourse().getId()))
                .collect(Collectors.toList());

        // Tạo map để đếm số học viên theo ngày
        Map<String, Long> studentsByDay = new LinkedHashMap<>();
        
        // Khởi tạo tất cả các ngày với giá trị 0
        for (int i = 0; i < days; i++) {
            LocalDateTime date = startDate.plusDays(i);
            String dayKey = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            studentsByDay.put(dayKey, 0L);
        }

        // Đếm số học viên theo ngày
        for (Enrollment enrollment : enrollments) {
            String dayKey = enrollment.getStartAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            if (studentsByDay.containsKey(dayKey)) {
                studentsByDay.put(dayKey, studentsByDay.get(dayKey) + 1);
            }
        }

        // Chuyển đổi sang list data points
        List<StudentsChartDto.StudentDataPoint> dataPoints = new ArrayList<>();
        int dayIndex = 1;
        for (Map.Entry<String, Long> entry : studentsByDay.entrySet()) {
            dataPoints.add(StudentsChartDto.StudentDataPoint.builder()
                    .name("Ngày " + dayIndex)
                    .students(entry.getValue())
                    .build());
            dayIndex++;
        }

        return dataPoints;
    }

    /**
     * Lấy số lượng học viên theo từng tháng
     */
    private List<StudentsChartDto.StudentDataPoint> getStudentsByMonths(
            UUID instructorId, LocalDateTime startDate, LocalDateTime endDate, 
            int months, boolean useMonthNumber) {
        
        // Lấy tất cả courses của instructor
        List<Course> courses = courseRepository.findByInstructorId(instructorId, Pageable.unpaged()).getContent();
        List<UUID> courseIds = courses.stream().map(Course::getId).collect(Collectors.toList());

        // Lấy tất cả enrollments trong khoảng thời gian
        List<Enrollment> enrollments = enrollmentRepository.findByStartAtBetween(startDate, endDate);
        
        // Lọc chỉ lấy enrollments của courses thuộc instructor
        enrollments = enrollments.stream()
                .filter(e -> courseIds.contains(e.getCourse().getId()))
                .collect(Collectors.toList());

        // Tạo map để đếm số học viên theo tháng
        Map<String, Long> studentsByMonth = new LinkedHashMap<>();
        
        // Khởi tạo tất cả các tháng với giá trị 0
        for (int i = 0; i < months; i++) {
            LocalDateTime monthDate = startDate.plusMonths(i);
            String monthKey = monthDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            studentsByMonth.put(monthKey, 0L);
        }

        // Đếm số học viên theo tháng
        for (Enrollment enrollment : enrollments) {
            String monthKey = enrollment.getStartAt().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            if (studentsByMonth.containsKey(monthKey)) {
                studentsByMonth.put(monthKey, studentsByMonth.get(monthKey) + 1);
            }
        }

        // Chuyển đổi sang list data points
        List<StudentsChartDto.StudentDataPoint> dataPoints = new ArrayList<>();
        int monthIndex = 1;
        for (Map.Entry<String, Long> entry : studentsByMonth.entrySet()) {
            String name = "T" + monthIndex;
            
            dataPoints.add(StudentsChartDto.StudentDataPoint.builder()
                    .name(name)
                    .students(entry.getValue())
                    .build());
            monthIndex++;
        }

        return dataPoints;
    }
}
