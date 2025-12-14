package com.vinaacademy.platform.feature.discussion.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.vinaacademy.platform.feature.discussion.dto.DiscussionSummaryDto;

public interface DiscussionRepositoryCustom {
    Page<DiscussionSummaryDto> findRootCommentSummariesWithPriority(UUID lessonId, UUID currentUserId, Pageable pageable);
    Page<DiscussionSummaryDto> findReplySummariesWithPriority(UUID parentId, UUID currentUserId, Pageable pageable);
}
