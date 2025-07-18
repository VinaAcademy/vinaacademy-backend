package com.vinaacademy.platform.feature.section.service;

import com.vinaacademy.platform.exception.BadRequestException;
import com.vinaacademy.platform.exception.NotFoundException;
import com.vinaacademy.platform.feature.course.entity.Course;
import com.vinaacademy.platform.feature.course.repository.CourseRepository;
import com.vinaacademy.platform.feature.instructor.repository.CourseInstructorRepository;
import com.vinaacademy.platform.feature.section.dto.SectionDto;
import com.vinaacademy.platform.feature.section.dto.SectionRequest;
import com.vinaacademy.platform.feature.section.entity.Section;
import com.vinaacademy.platform.feature.section.mapper.SectionMapper;
import com.vinaacademy.platform.feature.section.repository.SectionRepository;
import com.vinaacademy.platform.feature.user.auth.helpers.SecurityHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SectionServiceImpl implements SectionService {

    private final SectionRepository sectionRepository;
    private final CourseRepository courseRepository;
    private final CourseInstructorRepository courseInstructorRepository;
    private final SectionMapper sectionMapper;
    private final SecurityHelper securityHelper;

    @Override
    @Transactional(readOnly = true)
    public List<SectionDto> getSectionsByCourse(UUID courseId) {
        log.debug("Getting sections for course id: {}", courseId);
        Course course = findCourseById(courseId);
        List<Section> sections = sectionRepository.findByCourseOrderByOrderIndex(course);
        return sectionMapper.toDtoList(sections);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SectionDto> getSectionsByCourseSlug(String courseSlug) {
        log.debug("Getting sections for course slug: {}", courseSlug);
        Course course = findCourseBySlug(courseSlug);
        List<Section> sections = sectionRepository.findByCourseOrderByOrderIndex(course);
        return sectionMapper.toDtoList(sections);
    }

    @Override
    @Transactional(readOnly = true)
    public SectionDto getSectionById(UUID id) {
        log.debug("Getting section by id: {}", id);
        Section section = findSectionById(id);
        return sectionMapper.toDto(section);
    }

    @Override
    @Transactional
    public SectionDto createSection(SectionRequest request) {
        log.debug("Creating section: {}", request);
        Course course = findCourseById(request.getCourseId());

        // Check if user has permission to modify this course
        checkCoursePermission(course);

        // Luôn đặt section mới ở cuối danh sách, bỏ qua orderIndex được cung cấp
        List<Section> existingSections = sectionRepository.findByCourseOrderByOrderIndex(course);
        int newOrderIndex = existingSections.size(); // Đặt ở vị trí cuối cùng

        // Check for duplicate title in the same course
        if (sectionRepository.existsByTitleAndCourse(request.getTitle(), course)) {
            throw BadRequestException.message("Tiêu đề mục đã tồn tại trong khóa học này");
        }

        Section section = Section.createSection(
                null,
                course,
                request.getTitle(),
                newOrderIndex, // Luôn sử dụng index cuối cùng
                null
        );

        section = sectionRepository.save(section);
        log.info("Section created with id: {}", section.getId());

        return sectionMapper.toDto(section);
    }

    @Override
    @Transactional
    public SectionDto updateSection(UUID id, SectionRequest request) {
        log.debug("Updating section with id: {}", id);
        Section section = findSectionById(id);
        Course course = findCourseById(request.getCourseId());

        // Check if user has permission to modify this course
        checkCoursePermission(course);

        // Check if the order index is changing
        if (section.getOrderIndex() != request.getOrderIndex()) {
            handleOrderIndexChange(section, request.getOrderIndex(), course);
        }

        // Check for duplicate title in the same course (excluding this section)
        if (!section.getTitle().equals(request.getTitle()) &&
                sectionRepository.existsByTitleAndCourse(request.getTitle(), course)) {
            throw BadRequestException.message("Tiêu đề mục đã tồn tại trong khóa học này");
        }

        section.setTitle(request.getTitle());
        section.setOrderIndex(request.getOrderIndex());

        // Only change course if different from current course
        if (!section.getCourse().getId().equals(course.getId())) {
            section.setCourse(course);
        }

        section = sectionRepository.save(section);
        log.info("Section updated with id: {}", section.getId());

        return sectionMapper.toDto(section);
    }

    @Override
    @Transactional
    public void deleteSection(UUID id) {
        log.debug("Deleting section with id: {}", id);
        Section section = findSectionById(id);
        Course course = section.getCourse();
        int deletedOrderIndex = section.getOrderIndex();

        // Check if user has permission to modify this course
        checkCoursePermission(course);

        // Check if section has lessons
        if (!section.getLessons().isEmpty()) {
            throw BadRequestException.message("Không thể xóa mục có bài học. Xóa tất cả bài học trước");
        }

        sectionRepository.delete(section);

        // Update order index for sections after the deleted one
        List<Section> sectionsToUpdate = sectionRepository.findByCourseOrderByOrderIndex(course).stream()
                .filter(s -> s.getOrderIndex() > deletedOrderIndex)
                .collect(Collectors.toList());

        for (Section sectionToUpdate : sectionsToUpdate) {
            sectionToUpdate.setOrderIndex(sectionToUpdate.getOrderIndex() - 1);
            sectionRepository.save(sectionToUpdate);
        }

        log.info("Section deleted with id: {}", id);
    }

    @Override
    @Transactional
    public void reorderSections(UUID courseId, List<UUID> sectionIds) {
        log.debug("Reordering sections for course id: {}", courseId);
        Course course = findCourseById(courseId);
        reorderSectionsForCourse(course, sectionIds);
    }

    @Override
    @Transactional
    public void reorderSectionsBySlug(String courseSlug, List<UUID> sectionIds) {
        log.debug("Reordering sections for course slug: {}", courseSlug);
        Course course = findCourseBySlug(courseSlug);
        reorderSectionsForCourse(course, sectionIds);
    }

    private void reorderSectionsForCourse(Course course, List<UUID> sectionIds) {
        // Check if user has permission to modify this course
        checkCoursePermission(course);

        // Get all sections for the course
        List<Section> sections = sectionRepository.findByCourseOrderByOrderIndex(course);

        // Validate that all section IDs belong to the course
        Set<UUID> courseSectionIds = sections.stream().map(Section::getId).collect(Collectors.toSet());
        if (!courseSectionIds.containsAll(sectionIds)) {
            throw BadRequestException.message("Danh sách ID không hợp lệ");
        }

        // Validate that all sections are included
        if (sections.size() != sectionIds.size()) {
            throw BadRequestException.message("Danh sách không đầy đủ các mục");
        }

        // Create a map for quick lookup
        Map<UUID, Section> sectionMap = new HashMap<>();
        sections.forEach(section -> sectionMap.put(section.getId(), section));

        // Batch update all sections with their new order indices
        List<Section> updatedSections = new ArrayList<>();
        for (int i = 0; i < sectionIds.size(); i++) {
            UUID sectionId = sectionIds.get(i);
            Section section = sectionMap.get(sectionId);
            section.setOrderIndex(i);
            updatedSections.add(section);
        }

        // Save all sections in a batch operation
        sectionRepository.saveAll(updatedSections);

        log.info("Sections reordered for course: {}", course.getName());
    }

    private void handleOrderIndexChange(Section section, int newOrderIndex, Course course) {
        int oldOrderIndex = section.getOrderIndex();
        List<Section> sections = sectionRepository.findByCourseOrderByOrderIndex(course);

        // Di chuyển lên (giảm orderIndex)
        if (newOrderIndex < oldOrderIndex) {
            for (Section s : sections) {
                if (s.getId().equals(section.getId())) {
                    continue; // Bỏ qua section hiện tại
                }
                if (s.getOrderIndex() >= newOrderIndex && s.getOrderIndex() < oldOrderIndex) {
                    s.setOrderIndex(s.getOrderIndex() + 1);
                    sectionRepository.save(s);
                }
            }
        }
        // Di chuyển xuống (tăng orderIndex)
        else if (newOrderIndex > oldOrderIndex) {
            for (Section s : sections) {
                if (s.getId().equals(section.getId())) {
                    continue; // Bỏ qua section hiện tại
                }
                if (s.getOrderIndex() <= newOrderIndex && s.getOrderIndex() > oldOrderIndex) {
                    s.setOrderIndex(s.getOrderIndex() - 1);
                    sectionRepository.save(s);
                }
            }
        }

        // Cập nhật section hiện tại
        section.setOrderIndex(newOrderIndex);
        sectionRepository.save(section);
    }

    private Section findSectionById(UUID id) {
        return sectionRepository.findById(id)
                .orElseThrow(() -> NotFoundException.message("Không tìm thấy mục với id: " + id));
    }

    private Course findCourseById(UUID id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> NotFoundException.message("Không tìm thấy khóa học với id: " + id));
    }
    
    private Course findCourseBySlug(String slug) {
        return courseRepository.findBySlug(slug)
                .orElseThrow(() -> NotFoundException.message("Không tìm thấy khóa học với slug: " + slug));
    }
    
    private void checkCoursePermission(Course course) {
        UUID currentUserId = securityHelper.getCurrentUser().getId();
        
        // Check if user is instructor of the course
        boolean isInstructor = courseInstructorRepository.existsByInstructorIdAndCourseId(
                currentUserId, course.getId());
        
        if (!isInstructor) {
            throw BadRequestException.message("Bạn không có quyền chỉnh sửa khóa học này");
        }
    }
}
