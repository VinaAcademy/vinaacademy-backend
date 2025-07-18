package com.vinaacademy.platform.feature.video.mapper;

import com.vinaacademy.platform.feature.user.entity.User;
import com.vinaacademy.platform.feature.video.dto.VideoNoteDto;
import com.vinaacademy.platform.feature.video.dto.VideoNoteRequestDto;
import com.vinaacademy.platform.feature.video.entity.Video;
import com.vinaacademy.platform.feature.video.entity.VideoNote;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface VideoNoteMapper {
    VideoNoteMapper INSTANCE = Mappers.getMapper(VideoNoteMapper.class);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "videoId", source = "video.id")
    VideoNoteDto toDto(VideoNote videoNote);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "video", source = "video")
    VideoNote toEntity(VideoNoteRequestDto requestDto, User user, Video video);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "video", ignore = true)
    void updateEntityFromDto(VideoNoteRequestDto requestDto, @MappingTarget VideoNote videoNote);
}
