package com.vinaacademy.platform.feature.discussion.repository.projection;

import java.time.LocalDateTime;
import java.util.UUID;

public interface DiscussionSummary {
    UUID getId();
    UUID getLessonId();
    UUID getUserId();
    String getUserFullName();
    String getAvatarUrl();
    String getComment();
    UUID getParentCommentId();
    Long getReplyCount();
    Long getFavoriteCount();
    Boolean getLikedByCurrentUser();
    LocalDateTime getCreatedDate();
}
