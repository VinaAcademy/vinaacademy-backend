package com.vinaacademy.platform.feature.migration.data;

import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@UtilityClass
public class DiscussionData {

    private static final List<String> QUESTIONS = List.of(
            "Phần này mình chưa hiểu lắm, ai giải thích giúp mình với?",
            "Tại sao lại sử dụng phương pháp này thay vì cách khác?",
            "Mình gặp lỗi ở đoạn code này, có ai biết fix không?",
            "Bài giảng rất hay, cảm ơn thầy!",
            "Có tài liệu tham khảo nào cho phần này không ạ?",
            "Phút thứ 5:30 mình thấy hơi khó hiểu.",
            "Làm thế nào để áp dụng kiến thức này vào dự án thực tế?",
            "Mình nghĩ phần này nên cập nhật thêm thông tin mới.",
            "Có ai làm bài tập phần này chưa, cho mình tham khảo với?",
            "Giọng giảng viên rất dễ nghe, bài học bổ ích."
    );

    private static final List<String> REPLIES = List.of(
            "Bạn có thể xem lại bài trước để hiểu rõ hơn nhé.",
            "Mình cũng gặp vấn đề tương tự, đang hóng câu trả lời.",
            "Cảm ơn bạn đã chia sẻ!",
            "Theo mình hiểu thì là như thế này...",
            "Bạn thử search keyword này xem sao.",
            "Đoạn này cần chú ý kỹ mới hiểu được.",
            "Mình đã làm được rồi, inbox mình chỉ cho nhé.",
            "Đúng rồi đó bạn.",
            "Cảm ơn giảng viên đã giải đáp.",
            "Rất hữu ích!"
    );

    public static String getRandomQuestion() {
        return QUESTIONS.get(ThreadLocalRandom.current().nextInt(QUESTIONS.size()));
    }

    public static String getRandomReply() {
        return REPLIES.get(ThreadLocalRandom.current().nextInt(REPLIES.size()));
    }
}
