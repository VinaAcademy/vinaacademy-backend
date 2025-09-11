// DiscussionDto.java
package com.vinaacademy.platform.feature.discussion.dto;

import lombok.*;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscussionDto {
    private UUID id;
    private UUID lessonId;
    private UUID userId;
    private String comment;
    private UUID parentCommentId;
    private Long replyCount;
    private Long favoriteCount;         // tổng lượt like
    private boolean likedByCurrentUser;  // user hiện tại đã like?
}
