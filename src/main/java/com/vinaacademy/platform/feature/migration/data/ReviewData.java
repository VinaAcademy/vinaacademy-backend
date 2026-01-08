package com.vinaacademy.platform.feature.migration.data;

import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@UtilityClass
public class ReviewData {

    // Định nghĩa Map chứa các mẫu review theo số sao
    private static final Map<Integer, List<String>> REVIEW_SAMPLES = Map.of(
        5, List.of(
            "Khóa học tuyệt vời, kiến thức rất thực tế!",
            "Giảng viên dạy cực kỳ có tâm, hỗ trợ nhiệt tình.",
            "Nội dung đầy đủ, dễ hiểu, rất đáng tiền.",
            "Rất hài lòng với chất lượng bài giảng.",
            "Học xong áp dụng được ngay vào công việc."
        ),
        4, List.of(
            "Chất lượng tốt, tuy nhiên một số bài hơi nhanh.",
            "Kiến thức hay, video rõ nét, âm thanh ổn.",
            "Khá hài lòng, mong có thêm nhiều bài tập thực hành.",
            "Khóa học bổ ích, phù hợp với người mới bắt đầu.",
            "Mọi thứ đều ổn, hỗ trợ cũng khá nhanh."
        ),
        3, List.of(
            "Chất lượng tạm ổn, kiến thức hơi căn bản.",
            "Nội dung ở mức trung bình, chưa có nhiều đột phá.",
            "Cần cải thiện thêm về phần âm thanh của video.",
            "Bài giảng hơi lan man nhưng vẫn chấp nhận được.",
            "Mức giá này thì chất lượng như vậy là vừa đủ."
        ),
        2, List.of(
            "Nội dung hơi sơ sài, không như kỳ vọng.",
            "Giảng viên giải thích hơi khó hiểu ở phần nâng cao.",
            "Cần cập nhật lại nội dung mới hơn, hơi cũ rồi.",
            "Chất lượng video không ổn định lắm.",
            "Hỗ trợ phản hồi hơi chậm."
        ),
        1, List.of(
            "Quá tệ, không khuyến khích tham gia.",
            "Kiến thức quá cũ, giảng viên dạy hời hợt.",
            "Nội dung không giống như mô tả ban đầu.",
            "Phí tiền, không học được gì nhiều.",
            "Hệ thống thường xuyên bị lỗi, không vào học được."
        )
    );

    /**
     * Lấy ngẫu nhiên một nội dung review dựa trên số sao
     */
    public static String getRandomReview(int rating) {
        List<String> samples = REVIEW_SAMPLES.getOrDefault(rating, REVIEW_SAMPLES.get(5));
        int randomIndex = ThreadLocalRandom.current().nextInt(samples.size());
        return samples.get(randomIndex);
    }
}
