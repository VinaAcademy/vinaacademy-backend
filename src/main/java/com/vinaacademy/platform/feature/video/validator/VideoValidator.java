package com.vinaacademy.platform.feature.video.validator;

import com.vinaacademy.platform.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class VideoValidator {
    private final Tika tika;
    private static final List<String> ALLOWED_VIDEO_MIME = List.of(
            "video/mp4", "video/x-matroska", "video/webm", "video/avi", "video/quicktime"
    );
    private static final List<String> ALLOWED_EXTENSIONS = List.of("mp4", "mkv", "webm", "avi", "mov");
    @Value("${application.video.maxSize:104857600}")
    private long maxSize;

    public void validate(MultipartFile file) throws IOException {
        if (file.isEmpty()) throw BadRequestException.message("File rỗng");
        if (file.getSize() > maxSize) throw BadRequestException.message("Video quá lớn (>100MB)");

        String mimeType = tika.detect(file.getInputStream());
        if (!ALLOWED_VIDEO_MIME.contains(mimeType)) {
            throw BadRequestException.message("Loại video không hỗ trợ: " + mimeType);
        }

        String ext = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw BadRequestException.message("Định dạng file không hỗ trợ: " + ext);
        }
    }

    private String getExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(f.lastIndexOf('.') + 1).toLowerCase())
                .orElse("");
    }
}