package com.vinaacademy.platform.feature.video.service.impl;

import com.vinaacademy.platform.exception.UnauthorizedException;
import com.vinaacademy.platform.feature.common.exception.ResourceNotFoundException;
import com.vinaacademy.platform.feature.enrollment.repository.EnrollmentRepository;
import com.vinaacademy.platform.feature.user.UserRepository;
import com.vinaacademy.platform.feature.user.entity.User;
import com.vinaacademy.platform.feature.video.dto.VideoNoteDto;
import com.vinaacademy.platform.feature.video.dto.VideoNoteRequestDto;
import com.vinaacademy.platform.feature.video.entity.Video;
import com.vinaacademy.platform.feature.video.entity.VideoNote;
import com.vinaacademy.platform.feature.video.mapper.VideoNoteMapper;
import com.vinaacademy.platform.feature.video.repository.VideoNoteRepository;
import com.vinaacademy.platform.feature.video.repository.VideoRepository;
import com.vinaacademy.platform.feature.video.service.VideoNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VideoNoteServiceImpl implements VideoNoteService {

    private final VideoNoteRepository videoNoteRepository;
    private final UserRepository userRepository;
    private final VideoRepository videoRepository;


    @Override
    @Transactional
    public VideoNoteDto createVideoNote(User user, VideoNoteRequestDto requestDto) {
        // Thực hiện logic tạo ghi chú
        Video video = videoRepository.findById(requestDto.getVideoId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy video với ID: " + requestDto.getVideoId()));

        if (!videoRepository.isUserEnrolledInCourse(requestDto.getVideoId(), user.getId())) {
            throw new UnauthorizedException("Người dùng không có quyền truy cập video này");
        }

        VideoNote videoNote = VideoNote.builder()
                .user(user) // Gán trực tiếp đối tượng User
                .video(video)
                .timeStampSeconds(requestDto.getTimeStampSeconds())
                .noteText(requestDto.getNoteText())
                .build();

        VideoNote savedNote = videoNoteRepository.save(videoNote);

        return VideoNoteMapper.INSTANCE.toDto(savedNote);
    }

    @Override
    @Transactional
    public VideoNoteDto updateVideoNote(User user, Long noteId, VideoNoteRequestDto requestDto) {
        VideoNote videoNote = videoNoteRepository.findByIdAndUserId(noteId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ghi chú với ID: " + noteId));

        if (!videoNote.getVideo().getId().equals(requestDto.getVideoId())) {
            Video newVideo = videoRepository.findById(requestDto.getVideoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy video với ID: " + requestDto.getVideoId()));

            if (!videoRepository.isUserEnrolledInCourse(requestDto.getVideoId(), user.getId())) {
                throw new UnauthorizedException("Người dùng không có quyền truy cập video này");
            }

            videoNote.setVideo(newVideo);
        }

        VideoNoteMapper.INSTANCE.updateEntityFromDto(requestDto, videoNote);
        VideoNote updatedNote = videoNoteRepository.save(videoNote);

        return VideoNoteMapper.INSTANCE.toDto(updatedNote);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoNoteDto> getVideoNotesByVideoAndUser(User user, UUID videoId) {
        // Kiểm tra người dùng có quyền truy cập vào video
        if (!videoRepository.isUserEnrolledInCourse(videoId, user.getId())) {
            throw new ResourceNotFoundException("Người dùng không có quyền truy cập video với ID: " + videoId);
        }

        // Lấy danh sách ghi chú
        List<VideoNote> videoNotes = videoNoteRepository.findByVideoIdAndUserId(videoId, user.getId());
        return videoNotes.stream()
                .map(VideoNoteMapper.INSTANCE::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoNoteDto> getAllVideoNotesByUser(User user) {
        List<VideoNote> videoNotes = videoNoteRepository.findByUserId(user.getId());
        return videoNotes.stream()
                .map(VideoNoteMapper.INSTANCE::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public VideoNoteDto getVideoNoteById(User user, Long noteId) {
        VideoNote videoNote = videoNoteRepository.findByIdAndUserId(noteId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ghi chú với ID: " + noteId));

        return VideoNoteMapper.INSTANCE.toDto(videoNote);
    }

    @Override
    @Transactional
    public void deleteVideoNote(User user, Long noteId) {
        VideoNote videoNote = videoNoteRepository.findByIdAndUserId(noteId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ghi chú với ID: " + noteId));

        videoNoteRepository.delete(videoNote);
    }
}
