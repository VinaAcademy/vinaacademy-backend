package com.vinaacademy.platform.feature.instructor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO cho biểu đồ tiến độ học viên theo thời gian
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentsProgressChartDto {

    private List<ProgressDataPoint> data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProgressDataPoint {
        private String name;           // Tên thời điểm (T1, T2, ... hoặc Week 1, Week 2...)
        private Long inProgress;       // Số học viên đang học
        private Long completed;        // Số học viên đã hoàn thành
        private Long newEnrollments;   // Số đăng ký mới
    }
}
