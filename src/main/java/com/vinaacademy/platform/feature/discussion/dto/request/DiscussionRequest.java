// DiscussionRequest.java
package com.vinaacademy.platform.feature.discussion.dto.request;

import lombok.*;
import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscussionRequest {
	@NotNull(message = "Id của bài học không được để trống")
    private UUID lessonId;
	@NotBlank(message = "Thảo luận không được để trống")
	@Size(max = 2000, message = "Không được vượt quá 2000 ký tự")
    private String comment;
	
    private UUID parentCommentId;
}
