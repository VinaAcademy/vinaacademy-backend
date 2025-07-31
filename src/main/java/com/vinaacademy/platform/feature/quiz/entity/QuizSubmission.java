package com.vinaacademy.platform.feature.quiz.entity;

import com.vinaacademy.platform.feature.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "quiz_submissions")
public class QuizSubmission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "score")
    private double score;

    @Column(name = "total_points")
    private double totalPoints;

    @Column(name = "is_passed")
    private boolean isPassed;

    @OneToOne(mappedBy = "quizSubmission")
    private QuizSession quizSession;

    @Builder.Default
    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserAnswer> userAnswers = new ArrayList<>();

    public void addUserAnswer(UserAnswer userAnswer) {
        userAnswers.add(userAnswer);
        userAnswer.setSubmission(this);
    }

    public void removeUserAnswer(UserAnswer userAnswer) {
        userAnswers.remove(userAnswer);
        userAnswer.setSubmission(null);
    }
}