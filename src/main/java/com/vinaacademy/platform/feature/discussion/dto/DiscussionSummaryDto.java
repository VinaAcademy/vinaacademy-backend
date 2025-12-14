package com.vinaacademy.platform.feature.discussion.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscussionSummaryDto {
    private UUID id;
    private String comment;
    private UUID lessonId;
    private UUID userId;
    private Long replyCount;
    private Long favoriteCount;
    private String userFullName;
    private String avatarUrl;
    private LocalDateTime createdDate;
    private Boolean likedByCurrentUser;
}
