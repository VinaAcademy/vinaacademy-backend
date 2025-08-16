package com.vinaacademy.platform.feature.section.mapper;

import com.vinaacademy.platform.feature.section.dto.SectionDto;
import com.vinaacademy.platform.feature.section.dto.SectionRequest;
import com.vinaacademy.platform.feature.section.entity.Section;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * Mapper for the {@link Section} entity and its DTOs.
 */
@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SectionMapper {
    SectionMapper INSTANCE = Mappers.getMapper(SectionMapper.class);

    /**
     * Maps a Section entity to a SectionDto
     */
    @Mapping(target = "courseId", source = "course.id")
    @Mapping(target = "courseName", source = "course.name")
    @Mapping(target = "lessonCount", expression = "java(section.getLessons() != null ? section.getLessons().size() : 0)")
    SectionDto toDto(Section section);

    /**
     * Maps a list of Section entities to a list of SectionDtos
     */
    List<SectionDto> toDtoList(List<Section> sections);

    /**
     * Maps a SectionRequest to a Section entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "lessons", ignore = true)
    Section toEntity(SectionRequest request);

    /**
     * Updates a Section entity from a SectionRequest
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "lessons", ignore = true)
    void updateFromRequest(SectionRequest request, @MappingTarget Section section);
}
