package com.vinaacademy.platform.feature.migration.data;

import lombok.experimental.UtilityClass;

import java.util.Map;

@UtilityClass
public class VideoData {
    public static final String JAVA_VIDEO_HLS_PATH = "videos/hls/d8862e3e-43e2-4534-a321-327f19a91f72";
    public static final String BOND_VIDEO_HLS_PATH = "videos/hls/3684e765-6c2f-4244-a080-642dfd9c71de";
    public static final String LINEAR_TRANSFORMATION_VIDEO_HLS_PATH = "videos/hls/e50e0649-cb20-407b-b5ae-1ce3a67d53bb";

    public static final Map<String, Double> VIDEO_DURATIONS = Map.of(
            JAVA_VIDEO_HLS_PATH, 141.200544,
            BOND_VIDEO_HLS_PATH, 1071.82075,
            LINEAR_TRANSFORMATION_VIDEO_HLS_PATH, 285.837642
    );

    public static String selectAppropriateVideoTitle(String courseName, String categoryName) {
        if (isProgrammingCategory(categoryName)) {
            return "Giới thiệu tổng quan về " + courseName;
        } else if (isBusinessCategory(categoryName)) {
            return "Khởi động: Những điều cần biết về " + courseName;
        } else {
            return "Bài học 1: Nhập môn " + courseName;
        }
    }

    public static String selectAppropriateVideoHlsPath(String courseName, String categoryName) {
        if (isProgrammingCategory(courseName) || isProgrammingCategory(categoryName)) {
            return JAVA_VIDEO_HLS_PATH;
        } else if (isBusinessCategory(courseName) || isBusinessCategory(categoryName)) {
            return BOND_VIDEO_HLS_PATH;
        } else {
            return LINEAR_TRANSFORMATION_VIDEO_HLS_PATH;
        }
    }

    private static boolean isProgrammingCategory(String name) {
        if (name == null) return false;
        String lower = name.toLowerCase();
        return lower.contains("lập trình") || lower.contains("cntt") || lower.contains("it") ||
                lower.contains("web") || lower.contains("java") || lower.contains("python") ||
                lower.contains("software") || lower.contains("công nghệ") || lower.contains("phần mềm") ||
                lower.contains("backend") || lower.contains("frontend") || lower.contains("data") ||
                lower.contains("ngôn ngữ") || lower.contains("phát triển");
    }

    private static boolean isBusinessCategory(String name) {
        if (name == null) return false;
        String lower = name.toLowerCase();
        return lower.contains("kinh tế") || lower.contains("tài chính") || lower.contains("đầu tư") ||
                lower.contains("chứng khoán") || lower.contains("marketing") || lower.contains("business") ||
                lower.contains("quản trị") || lower.contains("khởi nghiệp") || lower.contains("doanh nghiệp");
    }
}
