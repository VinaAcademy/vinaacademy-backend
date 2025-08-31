package com.vinaacademy.platform.feature.course.entity;

import com.vinaacademy.platform.feature.cart.entity.CartItem;
import com.vinaacademy.platform.feature.category.Category;
import com.vinaacademy.platform.feature.common.entity.BaseEntity;
import com.vinaacademy.platform.feature.course.enums.CourseLevel;
import com.vinaacademy.platform.feature.course.enums.CourseStatus;
import com.vinaacademy.platform.feature.enrollment.Enrollment;
import com.vinaacademy.platform.feature.instructor.CourseInstructor;
import com.vinaacademy.platform.feature.order_payment.entity.OrderItem;
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

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
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
  private BigDecimal price = BigDecimal.ZERO;

  @Column(name = "level")
  @Enumerated(EnumType.STRING)
  private CourseLevel level = CourseLevel.BEGINNER;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private CourseStatus status = CourseStatus.DRAFT;

  @Column(name = "language")
  private String language = "Tiếng Việt";

  @ManyToOne
  @JoinColumn(name = "category_id")
  private Category category;

  @Column(name = "rating")
  private double rating = 0.0;

  @Column(name = "total_rating")
  private long totalRating = 0;

  @Column(name = "total_student")
  private long totalStudent = 0;

  @Column(name = "total_section")
  private long totalSection = 0;

  @Column(name = "total_lesson")
  private long totalLesson = 0;

  @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderColumn(name = "order_index")
  private List<Section> sections = new ArrayList<>();

  /**
   * Adds a section to this course and updates the stored section and lesson aggregates.
   *
   * The section is appended to the course's sections list and its course reference is set
   * to this course. After adding, totalSection is set to the current number of sections
   * and totalLesson is recalculated as the sum of all lessons across every section.
   *
   * @param section the section to add to this course
   */
  public void addSection(Section section) {
    sections.add(section);
    section.setCourse(this);
    totalSection = sections.size();
    totalLesson = sections.stream().mapToLong(s -> s.getLessons().size()).sum();
  }

  /**
   * Removes a section from this course and updates section/lesson counters.
   *
   * Removes the given Section from the course's sections list, clears the
   * section's reference to this course, updates totalSection to the current
   * number of sections, and recomputes totalLesson as the sum of lessons in
   * all remaining sections.
   *
   * @param section the Section to remove; may be null or not present in the list,
   *                in which case this method is a no-op for list membership but
   *                will still attempt to clear the section's course reference
   */
  public void removeSection(Section section) {
    sections.remove(section);
    section.setCourse(null);
    totalSection = sections.size();
    totalLesson = sections.stream().mapToLong(s -> s.getLessons().size()).sum();
  }

  /**
   * Adds an enrollment to this course.
   *
   * Initializes the enrollments list if necessary, adds the given enrollment,
   * sets the enrollment's course reference to this course (maintaining the
   * bidirectional association), and updates the course's totalStudent counter.
   *
   * @param enrollment the enrollment to add; must not be null
   */
  public void addEnrollment(Enrollment enrollment) {
    if (enrollments == null) {
      enrollments = new ArrayList<>();
    }
    enrollments.add(enrollment);
    enrollment.setCourse(this);
    totalStudent = enrollments.size();
  }

  /**
   * Removes an enrollment from this course, clears the enrollment's course reference,
   * and updates the course's totalStudent counter.
   *
   * If the course has no enrollments list (null), the method is a no-op.
   *
   * @param enrollment the Enrollment to remove; may be null (no-op) or an instance associated with this course
   */
  public void removeEnrollment(Enrollment enrollment) {
    if (enrollments != null) {
      enrollments.remove(enrollment);
      enrollment.setCourse(null);
      totalStudent = enrollments.size();
    }
  }

  /**
   * Adds a review to this course and updates the course's aggregate rating.
   *
   * The given review is appended to the course's review list, its `course` reference
   * is set to this course, and the course rating/totalRating are recomputed.
   *
   * @param review the CourseReview to add; its `course` field will be set to this instance
   */
  public void addReview(CourseReview review) {
    courseReviews.add(review);
    review.setCourse(this);
    recalculateRating();
  }

  /**
   * Removes the given review from this course, clears the review's course reference, and recalculates the course's rating and rating count.
   *
   * @param review the non-null CourseReview to remove; its reference to this course will be cleared and the course rating will be updated
   */
  public void removeReview(CourseReview review) {
    courseReviews.remove(review);
    review.setCourse(null);
    recalculateRating();
  }

  /**
   * Recomputes this course's aggregate rating and rating count from its reviews.
   *
   * If there are no reviews, sets both {@code rating} and {@code totalRating} to zero.
   * Otherwise sets {@code totalRating} to the number of reviews and {@code rating}
   * to the arithmetic mean of all review ratings.
   */
  private void recalculateRating() {
    if (courseReviews.isEmpty()) {
      rating = 0.0;
      totalRating = 0;
    } else {
      totalRating = courseReviews.size();
      rating = courseReviews.stream().mapToDouble(CourseReview::getRating).average().orElse(0.0);
    }
  }

  @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
  private List<Enrollment> enrollments;

  @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
  @Fetch(FetchMode.SUBSELECT)
  @BatchSize(size = 50)
  @OrderColumn(name = "id")
  private List<CourseInstructor> instructors = new ArrayList<>();

  @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
  @OrderColumn(name = "id")
  private List<CartItem> cartItems;

  @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
  @Fetch(FetchMode.SUBSELECT)
  @OrderColumn(name = "created_date")
  @BatchSize(size = 50)
  private List<CourseReview> courseReviews = new ArrayList<>();

  @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
  @OrderColumn(name = "id")
  private List<OrderItem> orderItems;
}
