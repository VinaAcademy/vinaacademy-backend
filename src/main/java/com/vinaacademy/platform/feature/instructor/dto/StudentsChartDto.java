package com.vinaacademy.platform.feature.instructor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO cho biểu đồ số lượng học viên theo thời gian
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentsChartDto {

    private List<StudentDataPoint> data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentDataPoint {
        private String name;      // Tên thời điểm (T1, T2, ... hoặc Week 1, Week 2...)
        private Long students;    // Số lượng học viên mới
    }
}
