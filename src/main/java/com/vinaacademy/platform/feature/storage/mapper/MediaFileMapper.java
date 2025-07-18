package com.vinaacademy.platform.feature.storage.mapper;

import com.vinaacademy.platform.feature.storage.dto.MediaFileDto;
import com.vinaacademy.platform.feature.storage.entity.MediaFile;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MediaFileMapper {
    MediaFileMapper INSTANCE = Mappers.getMapper(MediaFileMapper.class);
    MediaFileDto toDto(MediaFile mediaFile);
}
