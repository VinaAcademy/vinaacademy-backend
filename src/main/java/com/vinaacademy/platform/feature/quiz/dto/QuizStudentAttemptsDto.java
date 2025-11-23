package com.vinaacademy.platform.feature.quiz.dto;

import com.vinaacademy.platform.feature.user.dto.UserDto;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizStudentAttemptsDto {
  private UserDto student;
  private List<QuizSubmissionResultDto> attempts = new ArrayList<>();
}
