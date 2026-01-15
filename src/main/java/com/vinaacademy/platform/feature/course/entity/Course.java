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

  @OneToOne(
      mappedBy = "course",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Builder.Default
  private CourseEstimatedTime estimatedTimeInfo = null;

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
    if (o == null) return false;
    // Avoid issues when comparing Hibernate proxies
    if (org.hibernate.Hibernate.getClass(this) != org.hibernate.Hibernate.getClass(o)) return false;
    Course course = (Course) o;
    // Do not treat two new (id == null) entities as equal
    return this.id != null && this.id.equals(course.id);
  }

  @Override
  public int hashCode() {
    // Stable across proxies and before persisting (when id is null)
    return getClass().hashCode();
  }

  /**
   * Convenience accessor to get estimated time with default fallback.
   */
  public Integer getEstimatedTime() {
    return estimatedTimeInfo != null && estimatedTimeInfo.getEstimatedTime() != null
        ? estimatedTimeInfo.getEstimatedTime()
        : 1;
  }

  /**
   * Upsert estimated time entry; keeps default 1 when null provided.
   */
  public void updateEstimatedTime(Integer estimatedTime) {
    Integer value = estimatedTime != null ? estimatedTime : 1;
    if (this.estimatedTimeInfo == null) {
      this.estimatedTimeInfo = CourseEstimatedTime.builder().course(this).estimatedTime(value).build();
    } else {
      this.estimatedTimeInfo.setEstimatedTime(value);
    }
  }
}
