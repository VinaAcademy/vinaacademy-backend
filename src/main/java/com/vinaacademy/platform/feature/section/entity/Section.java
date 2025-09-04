package com.vinaacademy.platform.feature.section.entity;

import com.vinaacademy.platform.feature.common.entity.BaseEntity;
import com.vinaacademy.platform.feature.course.entity.Course;
import com.vinaacademy.platform.feature.lesson.entity.Lesson;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
    @OrderColumn(name = "order_index")
    @Builder.Default
    private List<Lesson> lessons = new ArrayList<>();

    /**
     * Override equals and hashCode to prevent circular reference issues
     * Only use ID field for comparison to avoid infinite loops with bidirectional relationships
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || org.hibernate.Hibernate.getClass(this) != org.hibernate.Hibernate.getClass(o)) return false;
        Section that = (Section) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return (id != null)
            ? id.hashCode()
            : org.hibernate.Hibernate.getClass(this).hashCode();
    }

    @Override
    public String toString() {
        return "Section{" +
            "id=" + id +
            ", title='" + title + '\'' +
            ", orderIndex=" + orderIndex +
            '}';
    }

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
