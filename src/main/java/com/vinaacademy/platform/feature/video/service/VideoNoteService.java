package com.vinaacademy.platform.feature.video.service;

import com.vinaacademy.platform.feature.user.entity.User;
import com.vinaacademy.platform.feature.video.dto.VideoNoteDto;
import com.vinaacademy.platform.feature.video.dto.VideoNoteRequestDto;

import java.util.List;
import java.util.UUID;

public interface VideoNoteService {
    //Tạo ghi chú mới
    VideoNoteDto createVideoNote(User user, VideoNoteRequestDto requestDto);

    //Cập nhật ghi chú
    VideoNoteDto updateVideoNote(User user, Long noteId, VideoNoteRequestDto requestDto);

    //Lấy tất cả ghi chú của một người dùng cho một video cụ thể
    List<VideoNoteDto> getVideoNotesByVideoAndUser(User user, UUID videoId);

    //Lấy tất cả ghi chú của một người dùng
    List<VideoNoteDto> getAllVideoNotesByUser(User user);

    //Lấy thông tin chi tiết của một ghi chú
    VideoNoteDto getVideoNoteById(User user, Long noteId);

    //Xóa một ghi chú
    void deleteVideoNote(User user, Long noteId);

}
