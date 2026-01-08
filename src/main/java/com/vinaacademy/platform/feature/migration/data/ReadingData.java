package com.vinaacademy.platform.feature.migration.data;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ReadingData {

    public static String selectAppropriateReadingTitle(String courseName, String categoryName) {
        // default title
        return "Tài liệu học tập về " + categoryName;
    }

    /**
     * Select appropriate content based on the category name
     */
    public static String selectAppropriateContent(String courseName, String description, String categoryName) {
        // Check if this is a programming-related course
        if (categoryName.contains("lập trình") || categoryName.contains("Lập trình") ||
                categoryName.contains("ngôn ngữ") || categoryName.contains("IT") ||
                categoryName.contains("phần mềm") || categoryName.contains("web") ||
                categoryName.contains("CNTT") || categoryName.contains("phát triển")) {

            return ReadingData.createProgrammingReadingContent(courseName, categoryName);
        }

        // Check if this is a business/finance related course
        else if (categoryName.contains("kinh doanh") || categoryName.contains("Kinh doanh") ||
                categoryName.contains("tài chính") || categoryName.contains("Tài chính") ||
                categoryName.contains("quản lý") || categoryName.contains("Quản lý") ||
                categoryName.contains("tiếp thị") || categoryName.contains("Tiếp thị") ||
                categoryName.contains("marketing") || categoryName.contains("Marketing")) {
            return ReadingData.createBusinessReadingContent(courseName, categoryName);
        }

        // Default content for other categories
        return ReadingData.generateDefaultReadingContent(courseName, description, categoryName);
    }

    public String generateDefaultReadingContent(String courseName, String courseDescription, String categoryName) {
        return "<h1>" + courseName + "</h1>"

                // Course description
                + "<p>" + courseDescription + "</p>"

                // Course overview
                + "<h2>Tổng quan khóa học</h2>"
                + "<p>Khóa học toàn diện này trong lĩnh vực <strong>" + categoryName
                + "</strong> sẽ hướng dẫn bạn qua tất cả các khái niệm và kỹ năng thực tế cần thiết để thành thạo trong lĩnh vực này.</p>"

                // What you'll learn
                + "<h2>Bạn sẽ học được gì</h2>"
                + "<ul>"
                + "<li>Khái niệm và nguyên lý cơ bản của " + categoryName + "</li>"
                + "<li>Kỹ năng và kỹ thuật thực hành thông qua các bài tập</li>"
                + "<li>Chiến lược nâng cao cho ứng dụng thực tế</li>"
                + "<li>Quy tắc thực hành tốt và tiêu chuẩn ngành</li>"
                + "<li>Giải quyết vấn đề và tư duy phản biện trong bối cảnh của "
                + categoryName + "</li>"
                + "</ul>"

                // Course structure
                + "<h2>Cấu trúc khóa học</h2>"
                + "<p>Khóa học này được chia thành nhiều module, mỗi module tập trung vào các khía cạnh cụ thể của chủ đề. "
                + "Bạn sẽ được học lý thuyết sau đó là các bài tập thực hành để củng cố kiến thức.</p>"

                // Prerequisites
                + "<h2>Điều kiện tiên quyết</h2>"
                + "<p>Mặc dù khóa học này được thiết kế để dễ tiếp cận với người mới bắt đầu, việc có một số kiến thức cơ bản trong các lĩnh vực sau sẽ có lợi:</p>"
                + "<ul>"
                + "<li>Kỹ năng máy tính cơ bản</li>"
                + "<li>Hiểu biết nền tảng về các khái niệm " + categoryName + "</li>"
                + "<li>Sự nhiệt tình và sẵn lòng học hỏi!</li>"
                + "</ul>"

                // Assessment methods
                + "<h2>Phương pháp đánh giá</h2>"
                + "<p>Tiến độ của bạn sẽ được đánh giá thông qua:</p>"
                + "<ul>"
                + "<li>Bài kiểm tra cuối mỗi phần</li>"
                + "<li>Bài tập thực hành</li>"
                + "<li>Một bài đánh giá toàn diện cuối cùng</li>"
                + "</ul>"

                // Closing
                + "<p>Chúng tôi rất vui mừng khi có bạn tham gia vào hành trình học tập này. Hãy bắt đầu!</p>";
    }

    /**
     * Creates programming-specific reading content formatted for Tiptap editor
     *
     * @param courseName   The name of the course
     * @param categoryName The category name
     * @return HTML content compatible with Tiptap editor
     */
    public String createProgrammingReadingContent(String courseName, String categoryName) {

        // Main heading

        // Introduction section
        // Data structures and algorithms section
        // OOP principles section
        // Java example section
        // References section
        // Exercises section
        // Add author and date info - useful for course versioning

        return "<h1>" + courseName + "</h1>"

                // Introduction section
                + "<div>"
                + "<h2>Giới thiệu về lập trình trong " + categoryName + "</h2>"
                + "<p>Trong thế giới công nghệ ngày nay, việc thành thạo các kỹ năng lập trình là vô cùng quan trọng. "
                + "Khóa học này sẽ giúp bạn hiểu rõ và ứng dụng thành thạo những khái niệm lập trình quan trọng.</p>"
                + "</div>"

                // Data structures and algorithms section
                + "<div>"
                + "<h2>Cấu trúc dữ liệu và thuật toán</h2>"
                + "<pre><code># Ví dụ về thuật toán sắp xếp nhanh (Quick sort)\n"
                + "def quick_sort(arr):\n"
                + "    if len(arr) <= 1:\n"
                + "        return arr\n"
                + "    pivot = arr[len(arr) // 2]\n"
                + "    left = [x for x in arr if x < pivot]\n"
                + "    middle = [x for x in arr if x == pivot]\n"
                + "    right = [x for x in arr if x > pivot]\n"
                + "    return quick_sort(left) + middle + quick_sort(right)\n"
                + "\n"
                + "# Sử dụng ví dụ\n"
                + "mang_so = [3, 6, 8, 10, 1, 2, 1]\n"
                + "mang_da_sap_xep = quick_sort(mang_so)\n"
                + "print(\"Kết quả: \", mang_da_sap_xep)</code></pre>"
                + "</div>"

                // OOP principles section
                + "<div>"
                + "<h2>Nguyên lý lập trình hướng đối tượng</h2>"
                + "<p>Lập trình hướng đối tượng (OOP) là một phương pháp lập trình dựa trên khái niệm về \"đối tượng\".</p>"
                + "<p><strong>Các nguyên tắc cơ bản:</strong></p>"
                + "<ol>"
                + "<li><strong>Tính đóng gói (Encapsulation)</strong> - Ẩn dữ liệu thực thi chi tiết</li>"
                + "<li><strong>Tính kế thừa (Inheritance)</strong> - Cho phép lớp con kế thừa từ lớp cha</li>"
                + "<li><strong>Tính đa hình (Polymorphism)</strong> - Cho phép các đối tượng khác nhau phản ứng khác nhau với cùng một thông điệp</li>"
                + "<li><strong>Tính trừu tượng (Abstraction)</strong> - Ẩn sự phức tạp thông qua các giao diện đơn giản</li>"
                + "</ol>"
                + "</div>"

                // Java example section
                + "<div>"
                + "<h2>Ví dụ về lớp và đối tượng trong Java</h2>"
                + "<pre><code>public class NhanVien {\n"
                + "    // Thuộc tính\n"
                + "    private String hoTen;\n"
                + "    private int tuoi;\n"
                + "    private double luong;\n\n"
                + "    // Constructor\n"
                + "    public NhanVien(String hoTen, int tuoi, double luong) {\n"
                + "        this.hoTen = hoTen;\n"
                + "        this.tuoi = tuoi;\n"
                + "        this.luong = luong;\n"
                + "    }\n\n"
                + "    // Phương thức\n"
                + "    public void hienThiThongTin() {\n"
                + "        System.out.println(\"Họ tên: \" + hoTen);\n"
                + "        System.out.println(\"Tuổi: \" + tuoi);\n"
                + "        System.out.println(\"Lương: \" + luong);\n"
                + "    }\n"
                + "}</code></pre>"
                + "</div>"

                // References section
                + "<div>"
                + "<h2>Tài liệu tham khảo</h2>"
                + "<ul>"
                + "<li>Clean Code - Robert C. Martin</li>"
                + "<li>Design Patterns - Gang of Four</li>"
                + "<li>Effective Java - Joshua Bloch</li>"
                + "<li>Head First Design Patterns</li>"
                + "</ul>"
                + "</div>"

                // Exercises section
                + "<div>"
                + "<h2>Bài tập thực hành</h2>"
                + "<ol>"
                + "<li>Tạo một ứng dụng quản lý sinh viên đơn giản</li>"
                + "<li>Áp dụng các nguyên tắc OOP vào dự án của bạn</li>"
                + "<li>Tối ưu hóa một thuật toán sắp xếp để cải thiện hiệu suất</li>"
                + "</ol>"
                + "<p style=\"text-align: center;\"><strong>Chúc bạn học tập hiệu quả!</strong></p>"
                + "</div>"

                // Add author and date info - useful for course versioning
                + "<div>"
                + "<p><em>Tác giả: lochuung</em></p>"
                + "<p><em>Cập nhật lần cuối: 2025-05-07</em></p>"
                + "</div>";
    }


    /**
     * Creates business or finance specific reading content formatted for Tiptap editor
     *
     * @param courseName   The name of the course
     * @param categoryName The category name
     * @return HTML content compatible with Tiptap editor
     */
    public String createBusinessReadingContent(String courseName, String categoryName) {
        return "<h1>" + courseName + "</h1>"

                // Overview section
                + "<div>"
                + "<h2>Tổng quan về " + categoryName + "</h2>"
                + "<p>Trong môi trường kinh doanh cạnh tranh ngày nay, việc hiểu rõ và áp dụng các nguyên tắc quản lý "
                + "và chiến lược kinh doanh hiệu quả là chìa khóa để thành công. Khóa học này cung cấp những kiến thức "
                + "thiết yếu giúp bạn vững vàng trong lĩnh vực " + categoryName + ".</p>"
                + "</div>"

                // Market analysis section
                + "<div>"
                + "<h2>Phân tích thị trường</h2>"
                + "<p>Phân tích thị trường là một quy trình thiết yếu giúp doanh nghiệp hiểu rõ về:</p>"
                + "<ul>"
                + "<li>Xu hướng tiêu dùng hiện tại</li>"
                + "<li>Hành vi của khách hàng</li>"
                + "<li>Chiến lược của đối thủ cạnh tranh</li>"
                + "<li>Cơ hội và thách thức mới nổi</li>"
                + "</ul>"
                + "</div>"

                // SWOT matrix section
                + "<div>"
                + "<h2>Ma trận SWOT</h2>"
                + "<table>"
                + "<tr>"
                + "<th></th>"
                + "<th>Tích cực</th>"
                + "<th>Tiêu cực</th>"
                + "</tr>"
                + "<tr>"
                + "<th>Nội bộ</th>"
                + "<td><strong>Điểm mạnh</strong><br>"
                + "- Nguồn lực độc đáo<br>"
                + "- Công nghệ tiên tiến<br>"
                + "- Đội ngũ chuyên nghiệp"
                + "</td>"
                + "<td><strong>Điểm yếu</strong><br>"
                + "- Thiếu nguồn vốn<br>"
                + "- Quy trình chưa tối ưu<br>"
                + "- Hạn chế về năng lực"
                + "</td>"
                + "</tr>"
                + "<tr>"
                + "<th>Bên ngoài</th>"
                + "<td><strong>Cơ hội</strong><br>"
                + "- Thị trường mới<br>"
                + "- Đối tác tiềm năng<br>"
                + "- Xu hướng mới"
                + "</td>"
                + "<td><strong>Thách thức</strong><br>"
                + "- Đối thủ cạnh tranh<br>"
                + "- Quy định pháp luật<br>"
                + "- Biến động kinh tế"
                + "</td>"
                + "</tr>"
                + "</table>"
                + "</div>"

                // Pricing strategy section
                + "<div>"
                + "<h2>Chiến lược định giá</h2>"
                + "<p>Việc xây dựng chiến lược định giá hiệu quả là yếu tố then chốt quyết định thành công của doanh nghiệp. "
                + "Dưới đây là một số phương pháp phổ biến:</p>"
                + "<div>"
                + "<h3>1. Định giá dựa trên chi phí</h3>"
                + "<p>Tính toán chi phí sản xuất và thêm phần lợi nhuận mong muốn</p>"
                + "</div>"
                + "<div>"
                + "<h3>2. Định giá dựa trên giá trị</h3>"
                + "<p>Xác định mức giá dựa trên giá trị mà khách hàng nhận được</p>"
                + "</div>"
                + "<div>"
                + "<h3>3. Định giá cạnh tranh</h3>"
                + "<p>Đặt giá dựa trên mức giá của đối thủ cạnh tranh</p>"
                + "</div>"
                + "<div>"
                + "<h3>4. Định giá theo phân khúc</h3>"
                + "<p>Áp dụng các mức giá khác nhau cho các phân khúc khách hàng khác nhau</p>"
                + "</div>"
                + "</div>"

                // Business plan section
                + "<div>"
                + "<h2>Kế hoạch kinh doanh mẫu</h2>"
                + "<div>"
                + "<ol>"
                + "<li>Tóm tắt điều hành</li>"
                + "<li>Mô tả công ty</li>"
                + "<li>Phân tích thị trường</li>"
                + "<li>Tổ chức và quản lý</li>"
                + "<li>Dòng sản phẩm hoặc dịch vụ</li>"
                + "<li>Chiến lược marketing và bán hàng</li>"
                + "<li>Dự báo tài chính</li>"
                + "</ol>"
                + "</div>"
                + "</div>"

                // References section
                + "<div>"
                + "<h2>Tài liệu tham khảo</h2>"
                + "<ul>"
                + "<li>\"Khởi nghiệp tinh gọn\" - Eric Ries</li>"
                + "<li>\"Tư duy như những nhà kinh doanh vĩ đại\" - Nguyễn Phi Vân</li>"
                + "<li>\"Quản trị marketing\" - Philip Kotler</li>"
                + "<li>\"Chiến lược đại dương xanh\" - W. Chan Kim và Renée Mauborgne</li>"
                + "</ul>"
                + "</div>"

                // Exercises section
                + "<div>"
                + "<h2>Bài tập thực hành</h2>"
                + "<ol>"
                + "<li>Xây dựng kế hoạch kinh doanh cho một sản phẩm hoặc dịch vụ mới</li>"
                + "<li>Thực hiện phân tích SWOT cho một doanh nghiệp thực tế</li>"
                + "<li>Thiết kế chiến lược marketing cho một thương hiệu</li>"
                + "</ol>"
                + "<p style=\"text-align: center;\"><strong>Chúc bạn thành công trong học tập và phát triển sự nghiệp!</strong></p>"
                + "</div>"

                // Add author and date info - useful for course versioning
                + "<div>"
                + "<p><em>Tác giả: lochuung</em></p>"
                + "<p><em>Cập nhật lần cuối: 2025-05-07</em></p>"
                + "</div>";
    }
}
