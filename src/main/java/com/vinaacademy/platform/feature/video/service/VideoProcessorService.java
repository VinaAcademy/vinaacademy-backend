package com.vinaacademy.platform.feature.video.service;

import com.vinaacademy.platform.feature.request.ProcessVideoRequest;

import java.nio.file.Path;
import java.util.UUID;

public interface VideoProcessorService {

    /**
     * Xử lý video bằng input file, thực hiện encode HLS và tạo thumbnail.
     *
     * @param videoId   ID của video
     * @param inputFile Đường dẫn file gốc cần xử lý
     */
    void processVideo(UUID videoId, Path inputFile);

    /**
     * Xử lý video dựa trên request đầu vào (ví dụ: từ API trigger).
     *
     * @param processVideoRequest request chứa thông tin xử lý video
     */
    void processVideo(ProcessVideoRequest processVideoRequest);
}
