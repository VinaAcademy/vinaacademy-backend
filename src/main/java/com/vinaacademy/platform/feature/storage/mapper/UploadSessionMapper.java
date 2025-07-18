package com.vinaacademy.platform.feature.storage.mapper;

import com.vinaacademy.platform.feature.storage.dto.UploadSessionDto;
import com.vinaacademy.platform.feature.storage.entity.MediaFile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface UploadSessionMapper {
    UploadSessionMapper INSTANCE = Mappers.getMapper(UploadSessionMapper.class);

    @Mapping(source = "id", target = "sessionId")
    UploadSessionDto toDto(MediaFile uploadSession);

    List<UploadSessionDto> toDtoList(List<MediaFile> activeSessions);
}
