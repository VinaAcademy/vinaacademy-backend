package com.vinaacademy.platform.feature.quiz.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vinaacademy.platform.feature.common.entity.BaseEntity;
import com.vinaacademy.platform.feature.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "quiz_sessions")
public class QuizSession extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "expiry_time")
    private LocalDateTime expiryTime;

//    @Column(name = "attempt_number", nullable = false)
//    private int attemptNumber = 1;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "quiz_submission_id")
    private QuizSubmission quizSubmission;

    public static QuizSession createNewSession(Quiz quiz, User user) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = null;

        // If quiz has time limit, calculate expiry time
        if (quiz.getTimeLimit() > 0) {
            expiry = now.plusMinutes(quiz.getTimeLimit());
        }

        return QuizSession.builder()
                .quiz(quiz)
                .user(user)
                .startTime(now)
                .active(true)
                .expiryTime(expiry)
                .build();
    }

    public boolean isSubmitted() {
        return quizSubmission != null && quizSubmission.getId() != null;
    }

    public boolean isExpired() {
        if (expiryTime == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(expiryTime);
    }
}