package com.vinaacademy.platform.feature.user;

import com.vinaacademy.platform.feature.user.auth.dto.RegisterRequest;
import com.vinaacademy.platform.feature.user.dto.UserDto;
import com.vinaacademy.platform.feature.user.dto.UserViewDto;
import com.vinaacademy.platform.feature.user.dto.ViewMappingDto;
import com.vinaacademy.platform.feature.user.entity.User;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    User toUser(RegisterRequest registerRequest);

    UserDto toDto(User user);

    @Mapping(expression = "java(viewMappingDto.countCourseCreate)", target = "countCourseCreate")
    @Mapping(expression = "java(viewMappingDto.countCourseEnroll)", target = "countCourseEnroll")
    @Mapping(expression = "java(viewMappingDto.countCourseEnrollComplete)", target = "countCourseEnrollComplete")
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isCollaborator", ignore = true) 
    UserViewDto toViewDto(User user, @Context ViewMappingDto viewMappingDto);  

}
