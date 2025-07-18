package com.vinaacademy.platform.feature.section.service;

import com.vinaacademy.platform.feature.section.dto.SectionDto;
import com.vinaacademy.platform.feature.section.dto.SectionRequest;

import java.util.List;
import java.util.UUID;

public interface SectionService {
    List<SectionDto> getSectionsByCourse(UUID courseId);
    List<SectionDto> getSectionsByCourseSlug(String courseSlug);
    SectionDto getSectionById(UUID id);
    SectionDto createSection(SectionRequest request);
    SectionDto updateSection(UUID id, SectionRequest request);
    void deleteSection(UUID id);
    void reorderSections(UUID courseId, List<UUID> sectionIds);
    void reorderSectionsBySlug(String courseSlug, List<UUID> sectionIds);
}
