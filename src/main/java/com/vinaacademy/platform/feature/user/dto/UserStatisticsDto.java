package com.vinaacademy.platform.feature.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user statistics in admin dashboard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatisticsDto {
    private long totalUsers;
    private long totalStudents;
    private long totalInstructors;
    private long totalAdmins;
    private long totalStaff;
    
    private long activeUsers;
    private long inactiveUsers;
    private long lockedUsers;
    
    private long newUsersToday;
    private long newUsersThisWeek;
    private long newUsersThisMonth;
    
    private long collaborators;
}
