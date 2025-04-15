package com.vinaacademy.platform.feature.quiz.entity;

import com.vinaacademy.platform.feature.common.entity.BaseEntity;
import com.vinaacademy.platform.feature.user.entity.User;
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
    
    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
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