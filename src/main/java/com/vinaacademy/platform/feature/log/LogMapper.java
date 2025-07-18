package com.vinaacademy.platform.feature.log;

import com.vinaacademy.platform.feature.log.dto.LogDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface LogMapper {
    LogMapper INSTANCE = Mappers.getMapper(LogMapper.class);

    Log toEntity(LogDto logDto);

    LogDto toDto(Log log);
}	
