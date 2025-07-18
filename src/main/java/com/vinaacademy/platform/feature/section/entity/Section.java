package com.vinaacademy.platform.feature.section.entity;

import com.vinaacademy.platform.feature.common.entity.BaseEntity;
import com.vinaacademy.platform.feature.course.entity.Course;
import com.vinaacademy.platform.feature.lesson.entity.Lesson;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Builder
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sections")
public class Section extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne()
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "title")
    private String title;

    @Column(name = "order_index")
    private int orderIndex;

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Lesson> lessons = new ArrayList<>();

    public void addLesson(Lesson lesson) {
        if (lessons == null) {
            lessons = new ArrayList<>();
        }
        lessons.add(lesson);
        lesson.setSection(this);
    }

    public void removeLesson(Lesson lesson) {
        if (lessons != null) {
            lessons.remove(lesson);
            lesson.setSection(null);
        }
    }
    
    // Custom builder to ensure the lessons list is always initialized
    @Builder
    public static Section createSection(UUID id, Course course, String title, int orderIndex, List<Lesson> lessons) {
        Section section = new Section();
        section.setId(id);
        section.setCourse(course);
        section.setTitle(title);
        section.setOrderIndex(orderIndex);
        section.setLessons(Objects.requireNonNullElseGet(lessons, ArrayList::new));
        return section;
    }
}
