package com.vinaacademy.platform.feature.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAnswerRequest {
    private UUID questionId;
    private List<UUID> selectedAnswerIds = new ArrayList<>();
    private String textAnswer;
}