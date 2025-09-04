package com.vinaacademy.platform.feature.course.entity;

import com.vinaacademy.platform.feature.category.Category;
import com.vinaacademy.platform.feature.common.entity.BaseEntity;
import com.vinaacademy.platform.feature.course.enums.CourseLevel;
import com.vinaacademy.platform.feature.course.enums.CourseStatus;
import com.vinaacademy.platform.feature.enrollment.Enrollment;
import com.vinaacademy.platform.feature.instructor.CourseInstructor;
import com.vinaacademy.platform.feature.review.entity.CourseReview;
import com.vinaacademy.platform.feature.section.entity.Section;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "courses",
    indexes = {@Index(name = "inx_course_slug", columnList = "slug")})
public class Course extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "image")
  private String image;

  @Column(name = "name")
  private String name;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(unique = true, name = "slug")
  private String slug;

  @Column(name = "price")
  @Builder.Default
  private BigDecimal price = BigDecimal.ZERO;

  @Column(name = "level")
  @Enumerated(EnumType.STRING)
  @Builder.Default
  private CourseLevel level = CourseLevel.BEGINNER;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  @Builder.Default
  private CourseStatus status = CourseStatus.DRAFT;

  @Column(name = "language")
  @Builder.Default
  private String language = "Tiếng Việt";

  @ManyToOne
  @JoinColumn(name = "category_id")
  private Category category;

  @Column(name = "rating")
  @Builder.Default
  private double rating = 0.0;

  @Column(name = "total_rating")
  @Builder.Default
  private long totalRating = 0;

  @Column(name = "total_student")
  @Builder.Default
  private long totalStudent = 0;

  @Column(name = "total_section")
  @Builder.Default
  private long totalSection = 0;

  @Column(name = "total_lesson")
  @Builder.Default
  private long totalLesson = 0;

  @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderColumn(name = "order_index")
  @Builder.Default
  private List<Section> sections = new ArrayList<>();

  @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
  private List<Enrollment> enrollments;

  @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
  @Fetch(FetchMode.SUBSELECT)
  @BatchSize(size = 50)
  @OrderBy("id ASC")
  @Builder.Default
  private List<CourseInstructor> instructors = new ArrayList<>();

  @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
  @Fetch(FetchMode.SUBSELECT)
  @BatchSize(size = 50)
  @OrderBy("createdDate DESC")
  @Builder.Default
  private List<CourseReview> courseReviews = new ArrayList<>();

  /**
   * Override equals and hashCode to prevent circular reference issues
   * Only use ID field for comparison to avoid infinite loops with bidirectional relationships
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Course course = (Course) o;
    return Objects.equals(id, course.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "Course{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", slug='" + slug + '\'' +
        ", status=" + status +
        ", level=" + level +
        ", price=" + price +
        ", rating=" + rating +
        ", totalStudent=" + totalStudent +
        ", totalSection=" + totalSection +
        ", totalLesson=" + totalLesson +
        '}';
  }
}
