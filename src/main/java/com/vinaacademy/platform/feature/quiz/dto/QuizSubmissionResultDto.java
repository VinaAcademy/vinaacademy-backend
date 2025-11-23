package com.vinaacademy.platform.feature.quiz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizSubmissionResultDto {
    private UUID id;
    private UUID quizId;
    private String quizTitle;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double score;
    private Double totalPoints;
    @JsonProperty("isPassed")
    private boolean isPassed;
    private List<UserAnswerResultDto> answers = new ArrayList<>();
}