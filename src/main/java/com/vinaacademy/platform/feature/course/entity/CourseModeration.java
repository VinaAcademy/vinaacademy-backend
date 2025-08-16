package com.vinaacademy.platform.feature.course.entity;

import com.vinaacademy.platform.feature.course.constants.CourseModerationStatus;
import com.vinaacademy.platform.feature.user.entity.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "course_moderation")
public class CourseModeration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Course course;

    @ManyToOne
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    private int status = CourseModerationStatus.PENDING;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "reviewed_at")
    @CreationTimestamp
    private LocalDateTime reviewedAt;
}
