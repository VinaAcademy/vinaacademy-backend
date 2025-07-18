package com.vinaacademy.platform.feature.video.mapper;

import com.vinaacademy.platform.feature.video.dto.VideoDto;
import com.vinaacademy.platform.feature.video.entity.Video;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

@Mapper
public interface VideoMapper {
    VideoMapper INSTANCE = Mappers.getMapper(VideoMapper.class);

    @Mapping(target = "videoId", source = "id")
    VideoDto toDto(Video video);

    default String map(UUID value) {
        return value != null ? value.toString() : null;
    }
}
