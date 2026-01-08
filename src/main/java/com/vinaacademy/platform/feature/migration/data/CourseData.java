package com.vinaacademy.platform.feature.migration.data;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CourseData {

    public static String selectAppropriateDescription(String courseName, String categoryName) {
        // You can expand this method to return different descriptions based on courseName or categoryName
        return generateDefaultDescription(courseName);
    }

    /**
     * Generates a default course description based on the course name in HTML format
     *
     * @param courseName The name of the course
     * @return A default description for the course in HTML
     */
    public static String generateDefaultDescription(String courseName) {

        return "<p>Chào mừng bạn đến với khóa học \"<strong>" + courseName + "</strong>\"! "
                + "Khóa học này được thiết kế để cung cấp cho bạn những kiến thức và kỹ năng toàn diện "
                + "giúp bạn trở nên thành thạo trong lĩnh vực này. "
                + "Từ các nguyên lý cơ bản đến các kỹ thuật nâng cao, khóa học sẽ đồng hành cùng bạn "
                + "trong suốt hành trình học tập và phát triển chuyên môn. "
                + "Với sự kết hợp giữa lý thuyết và thực hành, bạn sẽ được trang bị đầy đủ công cụ "
                + "để ứng dụng hiệu quả trong môi trường thực tế sau khi hoàn thành khóa học.</p>";
    }
}
