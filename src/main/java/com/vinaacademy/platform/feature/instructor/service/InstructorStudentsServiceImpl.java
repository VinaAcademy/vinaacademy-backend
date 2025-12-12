package com.vinaacademy.platform.feature.instructor.service;

import com.vinaacademy.platform.feature.course.entity.Course;
import com.vinaacademy.platform.feature.course.repository.CourseRepository;
import com.vinaacademy.platform.feature.enrollment.Enrollment;
import com.vinaacademy.platform.feature.enrollment.enums.ProgressStatus;
import com.vinaacademy.platform.feature.enrollment.repository.EnrollmentRepository;
import com.vinaacademy.platform.feature.instructor.dto.StudentDetailDto;
import com.vinaacademy.platform.feature.instructor.dto.StudentsOverviewDto;
import com.vinaacademy.platform.feature.instructor.dto.StudentsProgressChartDto;
import com.vinaacademy.platform.feature.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation của InstructorStudentsService
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InstructorStudentsServiceImpl implements InstructorStudentsService {

    private final UserService userService;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    public StudentsOverviewDto getStudentsOverview(String timeRange) {
        UUID instructorId = userService.getCurrentUser().getId();
        log.info("Getting students overview for instructor: {} with timeRange: {}", instructorId, timeRange);

        // Lấy tất cả course IDs của instructor
        List<UUID> courseIds = courseRepository.findByInstructorId(instructorId, Pageable.unpaged())
                .stream()
                .map(Course::getId)
                .collect(Collectors.toList());

        if (courseIds.isEmpty()) {
            return buildEmptyOverview();
        }

        // Xác định khoảng thời gian
        TimeRange range = getTimeRange(timeRange);

        // Tổng số học viên
        Long totalStudentsCount = enrollmentRepository.countDistinctUsersByCourseIds(courseIds);
        Long previousTotalStudents = getPreviousPeriodStudentCount(courseIds, range);
        BigDecimal totalGrowthRate = calculateGrowthRate(totalStudentsCount, previousTotalStudents);

        // Học viên mới trong kỳ hiện tại
        Long newStudentsCount = enrollmentRepository.countByCourseIdsAndDateRange(
                courseIds, range.currentStart, range.currentEnd);
        Long previousNewStudents = enrollmentRepository.countByCourseIdsAndDateRange(
                courseIds, range.previousStart, range.previousEnd);
        BigDecimal newGrowthRate = calculateGrowthRate(newStudentsCount, previousNewStudents);

        // Completion stats
        StudentsOverviewDto.CompletionStats completionStats = getCompletionStats(courseIds);

        // Top courses by students
        List<StudentsOverviewDto.CourseStudentCount> topCourses = getTopCoursesByStudents(courseIds);

        return StudentsOverviewDto.builder()
                .totalStudents(StudentsOverviewDto.StudentStats.builder()
                        .count(totalStudentsCount)
                        .growthRate(totalGrowthRate)
                        .build())
                .newStudents(StudentsOverviewDto.StudentStats.builder()
                        .count(newStudentsCount)
                        .growthRate(newGrowthRate)
                        .build())
                .completionStats(completionStats)
                .topCoursesByStudents(topCourses)
                .build();
    }

    @Override
    public Page<StudentDetailDto> getStudentsList(
            String courseId,
            ProgressStatus status,
            Double minProgress,
            Double maxProgress,
            String searchKeyword,
            Pageable pageable) {
        
        UUID instructorId = userService.getCurrentUser().getId();
        log.info("Getting students list for instructor: {}", instructorId);

        // Lấy course IDs
        List<UUID> courseIds;
        if (courseId != null && !courseId.isEmpty()) {
            courseIds = List.of(UUID.fromString(courseId));
        } else {
            courseIds = courseRepository.findByInstructorId(instructorId, Pageable.unpaged())
                    .stream()
                    .map(Course::getId)
                    .collect(Collectors.toList());
        }

        if (courseIds.isEmpty()) {
            return Page.empty(pageable);
        }

        // Query students với filters (status filter removed - shows all enrollments per user)
        Page<Object[]> studentsData = enrollmentRepository.findStudentsSummary(
                courseIds, minProgress, maxProgress, searchKeyword, pageable);

        List<StudentDetailDto> students = studentsData.getContent().stream()
                .map(row -> {
                    UUID userId = (UUID) row[0];
                    String fullName = (String) row[1];
                    String email = (String) row[2];
                    // avatarUrl removed - index 3 gone
                    Long enrollmentCount = ((Number) row[3]).longValue();
                    Double avgProgress = (Double) row[4];
                    // Native query returns Timestamp, convert to LocalDateTime
                    LocalDateTime lastActive = row[5] != null 
                            ? ((java.sql.Timestamp) row[5]).toLocalDateTime() 
                            : null;

                    // Lấy enrollments của user này
                    List<Enrollment> enrollments = enrollmentRepository.findByUserIdAndCourseIds(userId, courseIds);
                    
                    List<StudentDetailDto.CourseEnrollment> courseEnrollments = enrollments.stream()
                            .<StudentDetailDto.CourseEnrollment>map(e -> StudentDetailDto.CourseEnrollment.builder()
                                    .enrollmentId(e.getId())
                                    .courseId(e.getCourse().getId())
                                    .courseTitle(e.getCourse().getName())
                                    .courseThumbnail(e.getCourse().getImage())
                                    .status(e.getStatus())
                                    .progressPercentage(e.getProgressPercentage())
                                    .enrolledAt(e.getStartAt())
                                    .lastAccessedAt(lastActive) // Use lastActive from query
                                    .completedAt(e.getCompleteAt())
                                    .build())
                            .collect(Collectors.toList());

                    return StudentDetailDto.builder()
                            .userId(userId)
                            .fullName(fullName)
                            .email(email)
                            .avatar(null) // Temporarily null due to DB type issue
                            .enrollments(courseEnrollments)
                            .lastActive(lastActive)
                            .totalCoursesEnrolled(enrollmentCount.intValue())
                            .averageProgress(avgProgress)
                            .build();
                })
                .collect(Collectors.toList());

        return new PageImpl<>(students, pageable, studentsData.getTotalElements());
    }

    @Override
    public StudentsProgressChartDto getStudentsProgressChart(String period) {
        UUID instructorId = userService.getCurrentUser().getId();
        log.info("Getting students progress chart for instructor: {} with period: {}", instructorId, period);

        List<UUID> courseIds = courseRepository.findByInstructorId(instructorId, Pageable.unpaged())
                .stream()
                .map(Course::getId)
                .collect(Collectors.toList());

        if (courseIds.isEmpty()) {
            return StudentsProgressChartDto.builder().data(new ArrayList<>()).build();
        }

        LocalDateTime now = LocalDateTime.now();
        List<StudentsProgressChartDto.ProgressDataPoint> dataPoints;

        if ("WEEK".equalsIgnoreCase(period)) {
            dataPoints = getProgressByDays(courseIds, now.minusDays(6), now, 7);
        } else if ("YEAR".equalsIgnoreCase(period)) {
            LocalDateTime startDate = LocalDateTime.of(now.getYear(), 1, 1, 0, 0);
            LocalDateTime endDate = LocalDateTime.of(now.getYear(), 12, 31, 23, 59);
            dataPoints = getProgressByMonths(courseIds, startDate, endDate, 12);
        } else {
            // MONTH: 12 tháng gần nhất
            LocalDateTime startDate = now.minusMonths(11).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            dataPoints = getProgressByMonths(courseIds, startDate, now, 12);
        }

        return StudentsProgressChartDto.builder().data(dataPoints).build();
    }

    // Helper methods

    private StudentsOverviewDto buildEmptyOverview() {
        return StudentsOverviewDto.builder()
                .totalStudents(StudentsOverviewDto.StudentStats.builder()
                        .count(0L).growthRate(BigDecimal.ZERO).build())
                .newStudents(StudentsOverviewDto.StudentStats.builder()
                        .count(0L).growthRate(BigDecimal.ZERO).build())
                .completionStats(StudentsOverviewDto.CompletionStats.builder()
                        .inProgress(0L).completed(0L).notStarted(0L).averageCompletionRate(0.0).build())
                .topCoursesByStudents(new ArrayList<>())
                .build();
    }

    private TimeRange getTimeRange(String timeRange) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime currentStart, currentEnd, previousStart, previousEnd;

        switch (timeRange.toUpperCase()) {
            case "WEEK":
                currentStart = now.minusDays(7);
                currentEnd = now;
                previousStart = now.minusDays(14);
                previousEnd = now.minusDays(7);
                break;
            case "QUARTER":
                currentStart = now.minusDays(90);
                currentEnd = now;
                previousStart = now.minusDays(180);
                previousEnd = now.minusDays(90);
                break;
            case "YEAR":
                currentStart = now.minusYears(1);
                currentEnd = now;
                previousStart = now.minusYears(2);
                previousEnd = now.minusYears(1);
                break;
            case "ALL":
                currentStart = LocalDateTime.of(2000, 1, 1, 0, 0);
                currentEnd = now;
                previousStart = LocalDateTime.of(1999, 1, 1, 0, 0);
                previousEnd = LocalDateTime.of(2000, 1, 1, 0, 0);
                break;
            default: // MONTH
                currentStart = now.minusDays(30);
                currentEnd = now;
                previousStart = now.minusDays(60);
                previousEnd = now.minusDays(30);
        }

        return new TimeRange(currentStart, currentEnd, previousStart, previousEnd);
    }

    private Long getPreviousPeriodStudentCount(List<UUID> courseIds, TimeRange range) {
        // Count distinct students who enrolled before the current period
        return enrollmentRepository.countByCourseIdsAndDateRange(
                courseIds, range.previousStart, range.previousEnd);
    }

    private BigDecimal calculateGrowthRate(Long current, Long previous) {
        if (previous == null || previous == 0) {
            return current > 0 ? BigDecimal.valueOf(100) : BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(current - previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(previous), 2, RoundingMode.HALF_UP);
    }

    private StudentsOverviewDto.CompletionStats getCompletionStats(List<UUID> courseIds) {
        Long inProgress = enrollmentRepository.countByCourseIdsAndStatus(courseIds, ProgressStatus.IN_PROGRESS);
        Long completed = enrollmentRepository.countByCourseIdsAndStatus(courseIds, ProgressStatus.COMPLETED);
        Double avgProgress = enrollmentRepository.getAverageProgressByCourseIds(courseIds);

        return StudentsOverviewDto.CompletionStats.builder()
                .inProgress(inProgress != null ? inProgress : 0L)
                .completed(completed != null ? completed : 0L)
                .notStarted(0L)
                .averageCompletionRate(avgProgress != null ? avgProgress : 0.0)
                .build();
    }

    private List<StudentsOverviewDto.CourseStudentCount> getTopCoursesByStudents(List<UUID> courseIds) {
        List<Object[]> topCoursesData = enrollmentRepository.getTopCoursesByStudentCount(courseIds, 5);

        return topCoursesData.stream()
                .map(row -> StudentsOverviewDto.CourseStudentCount.builder()
                        .courseId(row[0].toString())
                        .courseTitle((String) row[1])
                        .courseThumbnail((String) row[2])
                        .studentCount(((Number) row[3]).longValue())
                        .build())
                .collect(Collectors.toList());
    }

    private List<StudentsProgressChartDto.ProgressDataPoint> getProgressByDays(
            List<UUID> courseIds, LocalDateTime startDate, LocalDateTime endDate, int days) {

        Map<String, StudentsProgressChartDto.ProgressDataPoint> dataByDay = new LinkedHashMap<>();

        // Initialize all days
        for (int i = 0; i < days; i++) {
            LocalDateTime date = startDate.plusDays(i);
            String dayKey = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            dataByDay.put(dayKey, StudentsProgressChartDto.ProgressDataPoint.builder()
                    .name("Ngày " + (i + 1))
                    .inProgress(0L)
                    .completed(0L)
                    .newEnrollments(0L)
                    .build());
        }

        // Get all enrollments for these courses
        List<Enrollment> allEnrollments = enrollmentRepository.findByCourseIdIn(courseIds);

        // For each day, count cumulative status and new enrollments
        for (int i = 0; i < days; i++) {
            LocalDateTime date = startDate.plusDays(i);
            LocalDateTime dayEnd = date.withHour(23).withMinute(59).withSecond(59);
            String dayKey = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            
            StudentsProgressChartDto.ProgressDataPoint point = dataByDay.get(dayKey);
            
            // Count enrollments that existed by end of this day
            long inProgressCount = allEnrollments.stream()
                    .filter(e -> e.getStartAt().isBefore(dayEnd) || e.getStartAt().isEqual(dayEnd))
                    .filter(e -> e.getStatus() == ProgressStatus.IN_PROGRESS)
                    .count();
            
            long completedCount = allEnrollments.stream()
                    .filter(e -> e.getStartAt().isBefore(dayEnd) || e.getStartAt().isEqual(dayEnd))
                    .filter(e -> e.getStatus() == ProgressStatus.COMPLETED)
                    .count();
            
            // Count new enrollments on this specific day
            long newCount = allEnrollments.stream()
                    .filter(e -> {
                        LocalDateTime enrollDate = e.getStartAt().withHour(0).withMinute(0).withSecond(0);
                        LocalDateTime checkDate = date.withHour(0).withMinute(0).withSecond(0);
                        return enrollDate.equals(checkDate);
                    })
                    .count();
            
            point.setInProgress(inProgressCount);
            point.setCompleted(completedCount);
            point.setNewEnrollments(newCount);
        }

        return new ArrayList<>(dataByDay.values());
    }

    private List<StudentsProgressChartDto.ProgressDataPoint> getProgressByMonths(
            List<UUID> courseIds, LocalDateTime startDate, LocalDateTime endDate, int months) {

        Map<String, StudentsProgressChartDto.ProgressDataPoint> dataByMonth = new LinkedHashMap<>();

        // Initialize all months
        for (int i = 0; i < months; i++) {
            LocalDateTime monthDate = startDate.plusMonths(i);
            String monthKey = monthDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            dataByMonth.put(monthKey, StudentsProgressChartDto.ProgressDataPoint.builder()
                    .name("T" + (i + 1))
                    .inProgress(0L)
                    .completed(0L)
                    .newEnrollments(0L)
                    .build());
        }

        // Get all enrollments for these courses
        List<Enrollment> allEnrollments = enrollmentRepository.findByCourseIdIn(courseIds);

        // For each month, count cumulative status and new enrollments
        for (int i = 0; i < months; i++) {
            LocalDateTime monthDate = startDate.plusMonths(i);
            LocalDateTime monthEnd = monthDate.withDayOfMonth(monthDate.toLocalDate().lengthOfMonth())
                    .withHour(23).withMinute(59).withSecond(59);
            String monthKey = monthDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            
            StudentsProgressChartDto.ProgressDataPoint point = dataByMonth.get(monthKey);
            
            // Count enrollments that existed by end of this month
            long inProgressCount = allEnrollments.stream()
                    .filter(e -> e.getStartAt().isBefore(monthEnd) || e.getStartAt().isEqual(monthEnd))
                    .filter(e -> e.getStatus() == ProgressStatus.IN_PROGRESS)
                    .count();
            
            long completedCount = allEnrollments.stream()
                    .filter(e -> e.getStartAt().isBefore(monthEnd) || e.getStartAt().isEqual(monthEnd))
                    .filter(e -> e.getStatus() == ProgressStatus.COMPLETED)
                    .count();
            
            // Count new enrollments in this specific month
            long newCount = allEnrollments.stream()
                    .filter(e -> {
                        String enrollMonth = e.getStartAt().format(DateTimeFormatter.ofPattern("yyyy-MM"));
                        return enrollMonth.equals(monthKey);
                    })
                    .count();
            
            point.setInProgress(inProgressCount);
            point.setCompleted(completedCount);
            point.setNewEnrollments(newCount);
        }

        return new ArrayList<>(dataByMonth.values());
    }

    private static class TimeRange {
        LocalDateTime currentStart;
        LocalDateTime currentEnd;
        LocalDateTime previousStart;
        LocalDateTime previousEnd;

        TimeRange(LocalDateTime currentStart, LocalDateTime currentEnd,
                  LocalDateTime previousStart, LocalDateTime previousEnd) {
            this.currentStart = currentStart;
            this.currentEnd = currentEnd;
            this.previousStart = previousStart;
            this.previousEnd = previousEnd;
        }
    }
}
