package com.vinaacademy.platform.feature.quiz.entity;

import com.vinaacademy.platform.feature.lesson.entity.Lesson;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Data
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("QUIZ")
public class Quiz extends Lesson {

    @Column(name = "totalPoint")
    private double totalPoints = 0;

    @Column(name = "duration")
    private int duration = 0;

    // Quiz settings
    @Column(name = "randomize_questions")
    private boolean randomizeQuestions = false;

    @Column(name = "show_correct_answers")
    private boolean showCorrectAnswers = true;

    @Column(name = "allow_retake")
    private boolean allowRetake = true;

    @Column(name = "require_passing_score")
    private boolean requirePassingScore = true;

    @Column(name = "passing_score")
    private double passingScore = 70.0;

    @Column(name = "time_limit")
    private int timeLimit = 0; // in minutes, 0 means no limit

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Question> questions = new ArrayList<>();

    public void addQuestion(Question question) {
        questions.add(question);
        question.setQuiz(this);
    }

    public void removeQuestion(Question question) {
        questions.remove(question);
        question.setQuiz(null);
    }
}
