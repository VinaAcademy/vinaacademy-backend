package com.vinaacademy.platform.feature.discussion.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

import com.vinaacademy.platform.feature.common.dto.BaseDto;
import com.vinaacademy.platform.feature.review.enums.FlagType;
import com.vinaacademy.platform.feature.review.enums.ModerationStatus;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DiscussionDto extends BaseDto{
    private UUID id;
    private UUID lessonId;
    private UUID userId;
    private String userFullName;
    private String avatarUrl; 
    private String comment;
    private UUID parentCommentId;
    private Long replyCount;
    private Long favoriteCount;         // tổng lượt like
    private boolean likedByCurrentUser;  // user hiện tại đã like?
    
    // Moderation fields
    private FlagType flagType;
    private ModerationStatus moderationStatus;
    private Integer flagSeverity;
}
