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
        String lowerCategory = categoryName.toLowerCase();
        String lowerCourse = (courseName != null) ? courseName.toLowerCase() : "";

        // Check if this is a programming-related course
        if (lowerCategory.contains("lập trình") || lowerCategory.contains("ngôn ngữ") ||
                lowerCategory.contains("it") || lowerCategory.contains("phần mềm") ||
                lowerCategory.contains("web") || lowerCategory.contains("cntt") ||
                lowerCategory.contains("phát triển") || lowerCategory.contains("java") ||
                lowerCategory.contains("python")) {

            return ReadingData.createProgrammingReadingContent(courseName, categoryName);
        }

        // Check if this is a business/finance related course
        else if (lowerCategory.contains("kinh doanh") || lowerCategory.contains("tài chính") ||
                lowerCategory.contains("quản lý") || lowerCategory.contains("tiếp thị") ||
                lowerCategory.contains("marketing") || lowerCategory.contains("khởi nghiệp")) {
            return ReadingData.createBusinessReadingContent(courseName, categoryName);
        }

        // Check if this is a design/multimedia related course
        else if (lowerCategory.contains("thiết kế") || lowerCategory.contains("đồ họa") ||
                lowerCategory.contains("video") || lowerCategory.contains("media") ||
                lowerCourse.contains("after effects") || lowerCourse.contains("premiere") ||
                lowerCourse.contains("camtasia") || lowerCourse.contains("adobe")) {
            return ReadingData.createDesignReadingContent(courseName, categoryName);
        }

        // Check if this is a music related course
        else if (lowerCategory.contains("âm nhạc") || lowerCategory.contains("nhạc") ||
                lowerCourse.contains("guitar") || lowerCourse.contains("piano") ||
                lowerCourse.contains("music") || lowerCourse.contains("hát")) {
            return ReadingData.createMusicReadingContent(courseName, categoryName);
        }

        // Check if this is a health/lifestyle related course
        else if (lowerCategory.contains("sức khỏe") || lowerCategory.contains("đời sống") ||
                lowerCategory.contains("yoga") || lowerCategory.contains("tâm lý") ||
                lowerCourse.contains("yoga") || lowerCourse.contains("psychology")) {
            return ReadingData.createHealthReadingContent(courseName, categoryName);
        }

        // Check if this is a language/academic related course
        else if (lowerCategory.contains("ngoại ngữ") || lowerCategory.contains("tiếng anh") ||
                lowerCategory.contains("học thuật") || lowerCourse.contains("toeic") ||
                lowerCourse.contains("english") || lowerCourse.contains("ielts") ||
                lowerCourse.contains("xác suất") || lowerCourse.contains("thống kê")) {
            return ReadingData.createAcademicReadingContent(courseName, categoryName);
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

    /**
     * Creates design, video editing, or multimedia specific reading content
     */
    public String createDesignReadingContent(String courseName, String categoryName) {
        return "<h1>" + courseName + "</h1>"
                + "<div>"
                + "<h2>Giới thiệu về Thiết kế & Đa phương tiện</h2>"
                + "<p>Trong kỷ nguyên số, khả năng tạo ra nội dung hình ảnh và video chất lượng cao là một kỹ năng được đánh giá rất cao. "
                + "Khóa học này sẽ hướng dẫn bạn sử dụng các công cụ mạnh mẽ để hiện thực hóa ý tưởng sáng tạo của mình.</p>"
                + "</div>"

                + "<div>"
                + "<h2>Nguyên tắc thiết kế cơ bản</h2>"
                + "<ul>"
                + "<li><strong>Cân bằng (Balance):</strong> Phân bố trọng lượng hình ảnh đều trong thiết kế.</li>"
                + "<li><strong>Tương phản (Contrast):</strong> Sử dụng sự khác biệt để tạo điểm nhấn.</li>"
                + "<li><strong>Khoảng trắng (White Space):</strong> Không gian nghỉ cho mắt, tăng tính thẩm mỹ.</li>"
                + "<li><strong>Đồng nhất (Consistency):</strong> Giữ phong cách nhất quán xuyên suốt.</li>"
                + "</ul>"
                + "</div>"

                + "<div>"
                + "<h2>Quy trình làm việc (Workflow) chuyên nghiệp</h2>"
                + "<ol>"
                + "<li><strong>Giai đoạn tiền kỳ (Pre-production):</strong> Lên ý tưởng, kịch bản, storyboard, chuẩn bị tài nguyên.</li>"
                + "<li><strong>Giai đoạn sản xuất (Production):</strong> Quay phim, chụp ảnh, vẽ minh họa, thiết kế layout.</li>"
                + "<li><strong>Giai đoạn hậu kỳ (Post-production):</strong> Chỉnh sửa, cắt ghép, thêm hiệu ứng, chỉnh màu (Color Grading).</li>"
                + "<li><strong>Xuất bản (Rendering/Export):</strong> Tối ưu hóa định dạng cho nền tảng mục tiêu.</li>"
                + "</ol>"
                + "</div>"

                + "<div>"
                + "<h2>Phím tắt quan trọng (Ví dụ Adobe)</h2>"
                + "<table>"
                + "<thead><tr><th>Chức năng</th><th>Phím tắt (Windows/Mac)</th></tr></thead>"
                + "<tbody>"
                + "<tr><td>Lưu dự án</td><td>Ctrl+S / Cmd+S</td></tr>"
                + "<tr><td>Hoàn tác (Undo)</td><td>Ctrl+Z / Cmd+Z</td></tr>"
                + "<tr><td>Sao chép (Copy)</td><td>Ctrl+C / Cmd+C</td></tr>"
                + "<tr><td>Dán (Paste)</td><td>Ctrl+V / Cmd+V</td></tr>"
                + "<tr><td>Công cụ chọn (Selection Tool)</td><td>V</td></tr>"
                + "<tr><td>Công cụ cắt (Razor Tool)</td><td>C</td></tr>"
                + "</tbody>"
                + "</table>"
                + "</div>"

                + "<div>"
                + "<h2>Bài tập thực hành</h2>"
                + "<ul>"
                + "<li>Tạo một video intro ngắn 15 giây giới thiệu bản thân.</li>"
                + "<li>Thiết kế một poster quảng cáo cho sự kiện giả định.</li>"
                + "<li>Thực hành chỉnh màu (Color correction) cho một đoạn footage thô (log footage).</li>"
                + "</ul>"
                + "</div>"

                + "<div>"
                + "<p><em>Hãy nhớ rằng: Sáng tạo là không giới hạn, công cụ chỉ là phương tiện để bạn thể hiện tư duy.</em></p>"
                + "<p><em>Tác giả: VinaAcademy Creative</em></p>"
                + "</div>";
    }

    /**
     * Creates music, theory, or instrument specific reading content
     */
    public String createMusicReadingContent(String courseName, String categoryName) {
        return "<h1>" + courseName + "</h1>"
                + "<div>"
                + "<h2>Khám phá thế giới âm nhạc</h2>"
                + "<p>Âm nhạc là ngôn ngữ chung của nhân loại. Khóa học này được thiết kế để giúp bạn không chỉ chơi nhạc cụ"
                + " mà còn hiểu sâu sắc về cấu trúc và cảm xúc trong âm nhạc.</p>"
                + "</div>"

                + "<div>"
                + "<h2>Lý thuyết âm nhạc căn bản</h2>"
                + "<p>Để chơi nhạc hay, bạn cần nắm vững:</p>"
                + "<ul>"
                + "<li><strong>Nhịp (Rhythm):</strong> Yếu tố thời gian trong âm nhạc (4/4, 3/4, etc.).</li>"
                + "<li><strong>Giai điệu (Melody):</strong> Chuỗi các nốt nhạc tạo nên 'tiếng hát' của bản nhạc.</li>"
                + "<li><strong>Hòa thanh (Harmony):</strong> Sự kết hợp của nhiều nốt nhạc cùng lúc (hợp âm).</li>"
                + "<li><strong>Âm sắc (Timbre):</strong> Màu sắc riêng biệt của từng nhạc cụ.</li>"
                + "</ul>"
                + "</div>"

                + "<div>"
                + "<h2>Cách đọc bản nhạc (Sheet Music) vs Tablature</h2>"
                + "<div style=\"display: flex; gap: 20px;\">"
                + "<div style=\"flex: 1;\"><h3>Bản nhạc (Sheet Music)</h3><p>Hệ thống ký hiệu chuẩn quốc tế, hiển thị cao độ và trường độ chính xác. Cần thiết cho Piano và dàn nhạc.</p></div>"
                + "<div style=\"flex: 1;\"><h3>Tablature (Guitar/Bass)</h3><p>Hệ thống ký hiệu vị trí ngón tay trên cần đàn. Dễ học, phổ biến trong nhạc Pop/Rock nhưng thiếu thông tin chi tiết về nhịp.</p></div>"
                + "</div>"
                + "</div>"

                + "<div>"
                + "<h2>Một số hợp âm phổ biến (C Major Key)</h2>"
                + "<ul>"
                + "<li><strong>C (Đô trưởng):</strong> C - E - G</li>"
                + "<li><strong>F (Fa trưởng):</strong> F - A - C</li>"
                + "<li><strong>G (Sol trưởng):</strong> G - B - D</li>"
                + "<li><strong>Am (La thứ):</strong> A - C - E</li>"
                + "</ul>"
                + "</div>"

                + "<div>"
                + "<h2>Luyện tập hàng ngày</h2>"
                + "<ol>"
                + "<li>Khởi động ngón tay (Warm-up) - 5 phút.</li>"
                + "<li>Luyện chạy ngón (Scales) - 10 phút.</li>"
                + "<li>Tập bài mới (Repertoire) - 20 phút.</li>"
                + "<li>Nghe và cảm nhận (Ear Training) - 10 phút.</li>"
                + "</ol>"
                + "</div>"

                + "<div>"
                + "<p><em>\"Âm nhạc thể hiện những điều không thể nói nhưng cũng không thể giữ im lặng.\" - Victor Hugo</em></p>"
                + "</div>";
    }

    /**
     * Creates health, yoga, or psychology specific reading content
     */
    public String createHealthReadingContent(String courseName, String categoryName) {
        return "<h1>" + courseName + "</h1>"
                + "<div>"
                + "<h2>Sức khỏe & Cân bằng thân tâm</h2>"
                + "<p>Chào mừng bạn đến với khóa học. Sức khỏe không chỉ là không có bệnh tật, mà là trạng thái hoàn toàn thoải mái về thể chất, tinh thần và xã hội.</p>"
                + "</div>"

                + "<div>"
                + "<h2>Lợi ích của việc thực hành đều đặn</h2>"
                + "<ul>"
                + "<li><strong>Cải thiện giấc ngủ:</strong> Giúp cơ thể thư giãn và phục hồi sâu.</li>"
                + "<li><strong>Giảm căng thẳng (Stress):</strong> Kích hoạt hệ thần kinh đối giao cảm.</li>"
                + "<li><strong>Tăng cường sự tập trung:</strong> Rèn luyện sự chú ý vào hiện tại (Mindfulness).</li>"
                + "<li><strong>Nâng cao độ dẻo dai:</strong> Cải thiện sức khỏe xương khớp và cơ bắp.</li>"
                + "</ul>"
                + "</div>"

                + "<div>"
                + "<h2>Kỹ thuật hít thở (Pranayama)</h2>"
                + "<p>Hơi thở là cầu nối giữa thân và tâm. Dưới đây là kỹ thuật thở bụng cơ bản:</p>"
                + "<ol>"
                + "<li>Ngồi thoải mái, giữ lưng thẳng.</li>"
                + "<li>Đặt tay lên bụng dưới.</li>"
                + "<li>Hít vào sâu qua mũi, cảm nhận bụng phình lên (3 giây).</li>"
                + "<li>Giữ hơi (2 giây).</li>"
                + "<li>Thở ra chậm rãi qua mũi, cảm nhận bụng xẹp xuống (4-5 giây).</li>"
                + "</ol>"
                + "</div>"

                + "<div>"
                + "<h2>Chế độ dinh dưỡng lành mạnh</h2>"
                + "<table>"
                + "<tr><th>Nên ăn</th><th>Hạn chế</th></tr>"
                + "<tr><td>Rau xanh, trái cây tươi</td><td>Đồ ăn chế biến sẵn</td></tr>"
                + "<tr><td>Ngũ cốc nguyên hạt</td><td>Đường tinh luyện</td></tr>"
                + "<tr><td>Protein từ thực vật/cá</td><td>Chất béo bão hòa xấu</td></tr>"
                + "<tr><td>Uống đủ nước</td><td>Đồ uống có gas, cồn</td></tr>"
                + "</table>"
                + "</div>"

                + "<div>"
                + "<h2>Lời khuyên cho người mới bắt đầu</h2>"
                + "<p>Hãy lắng nghe cơ thể mình. Không nên ép buộc cơ thể vào những tư thế hoặc bài tập quá sức. Sự tiến bộ đến từ sự kiên trì luyện tập hàng ngày (Abhyasa).</p>"
                + "</div>"

                + "<div>"
                + "<p><em>Namaste! Chúc bạn có những giây phút bình an.</em></p>"
                + "</div>";
    }

    /**
     * Creates academic, language learning specific reading content
     */
    public String createAcademicReadingContent(String courseName, String categoryName) {
        return "<h1>" + courseName + "</h1>"
                + "<div>"
                + "<h2>Giới thiệu môn học</h2>"
                + "<p>Khóa học này cung cấp kiến thức nền tảng và nâng cao về " + categoryName + ", giúp bạn đạt được kết quả cao trong các kỳ thi và ứng dụng thực tế.</p>"
                + "</div>"

                + "<div>"
                + "<h2>Phương pháp học tập hiệu quả</h2>"
                + "<ul>"
                + "<li><strong>Spaced Repetition (Lặp lại ngắt quãng):</strong> Ôn tập kiến thức theo chu kỳ để ghi nhớ lâu dài.</li>"
                + "<li><strong>Active Recall (Gợi nhớ chủ động):</strong> Tự kiểm tra thay vì chỉ đọc lại tài liệu.</li>"
                + "<li><strong>Pomodoro Technique:</strong> Học 25 phút, nghỉ 5 phút để duy trì sự tập trung.</li>"
                + "</ul>"
                + "</div>"

                + "<div>"
                + "<h2>Nội dung trọng tâm</h2>"
                + "<div style=\"background-color: #f5f5f5; padding: 15px; border-radius: 5px;\">"
                + "<h3>Từ vựng & Khái niệm (Key Vocabulary)</h3>"
                + "<ul>"
                + "<li><strong>Concept 1:</strong> Định nghĩa và ví dụ minh họa.</li>"
                + "<li><strong>Concept 2:</strong> Cách sử dụng trong ngữ cảnh cụ thể.</li>"
                + "<li><strong>Concept 3:</strong> Các lỗi sai thường gặp cần tránh.</li>"
                + "</ul>"
                + "</div>"
                + "</div>"

                + "<div>"
                + "<h2>Ví dụ minh họa</h2>"
                + "<blockquote>"
                + "Kiến thức không vĩ đại bằng sự áp dụng kiến thức. (Knowledge is of no value unless you put it into practice)."
                + "</blockquote>"
                + "<p>Ví dụ: Khi học từ mới, hãy đặt ngay một câu ví dụ với từ đó.</p>"
                + "</div>"

                + "<div>"
                + "<h2>Bài tập tự luyện (Self-Assessment)</h2>"
                + "<ol>"
                + "<li>Hoàn thành bài quiz trắc nghiệm cuối chương.</li>"
                + "<li>Viết một đoạn văn ngắn tóm tắt lại bài học hôm nay.</li>"
                + "<li>Giải quyết bài toán mẫu số 3, 4 trong tài liệu đính kèm.</li>"
                + "</ol>"
                + "</div>"

                + "<div>"
                + "<p><em>Chúc các bạn học tốt và đạt kết quả cao!</em></p>"
                + "</div>";
    }
}
