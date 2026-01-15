package com.vinaacademy.platform.feature.course.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "course_estimated_time")
public class CourseEstimatedTime {

  @Id
  @Column(name = "course_id")
  private UUID courseId;

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "course_id")
  private Course course;

  @Column(name = "estimated_time", nullable = false)
  @Builder.Default
  private Integer estimatedTime = 1;
}
