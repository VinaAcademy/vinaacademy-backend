package com.vinaacademy.platform.feature.lesson.entity;

import com.vinaacademy.platform.feature.common.entity.BaseEntity;
import com.vinaacademy.platform.feature.course.enums.LessonType;
import com.vinaacademy.platform.feature.section.entity.Section;
import com.vinaacademy.platform.feature.storage.entity.MediaFile;
import com.vinaacademy.platform.feature.user.entity.User;
import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "lesson_type", discriminatorType = DiscriminatorType.STRING)
@Table(name = "lessons")
public abstract class Lesson extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    protected UUID id;

    @ManyToOne
    @JoinColumn(name = "section_id", nullable = false)
    @ToString.Exclude
    protected Section section;

    @Column(name = "title")
    protected String title;

    @Column(name = "description", columnDefinition = "TEXT")
    protected String description;

    @Column(name = "lesson_type", nullable = false, insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    protected LessonType type = LessonType.READING;

    @Column(name = "is_free")
    protected boolean free = false;

    @Column(name = "order_index")
    protected int orderIndex;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    @ToString.Exclude
    protected User author;

    @Version
    @Column(name = "version")
    private Long version;

    @OneToMany(mappedBy = "lesson", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    @ToString.Exclude
    protected List<UserProgress> progressList;

    @ManyToMany
    @JoinTable(
            name = "lesson_media_files",
            joinColumns = @JoinColumn(name = "lesson_id"),
            inverseJoinColumns = @JoinColumn(name = "media_file_id")
    )
    @ToString.Exclude
    protected List<MediaFile> mediaFiles;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null
            || org.hibernate.Hibernate.getClass(this) != org.hibernate.Hibernate.getClass(o))
            return false;
        Lesson that = (Lesson) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        if (id == null) {
            return super.hashCode();
        }
        int result = super.hashCode();
        result = 31 * result + id.hashCode();
        return result;
    }
}
