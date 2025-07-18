package com.vinaacademy.platform.feature.enrollment;

import com.vinaacademy.platform.feature.course.entity.Course;
import com.vinaacademy.platform.feature.enrollment.enums.ProgressStatus;
import com.vinaacademy.platform.feature.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "enrollments", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "course_id"})
})
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "progress_percentage")
    @ColumnDefault("0.0")
    private double progressPercentage = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProgressStatus status = ProgressStatus.IN_PROGRESS;

    @Column(name = "start_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime startAt;

    @Column(name = "complete_at")
    private LocalDateTime completeAt;

    @Column(name = "completed_lessons", nullable = false)
    @ColumnDefault("0")
    private long completedLessons = 0;

    @Override
    public String toString() {
        return "Enrollment{" +
                "id=" + id +
                '}';
    }
}
