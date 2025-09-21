// DiscussionService.java
package com.vinaacademy.platform.feature.discussion.service;

import com.vinaacademy.platform.feature.discussion.dto.DiscussionDto;
import com.vinaacademy.platform.feature.discussion.dto.request.DiscussionRequest;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DiscussionService {
    DiscussionDto createDiscussion(DiscussionRequest request);
    Page<DiscussionDto> getRepliesWithReplyCount(UUID parentId, Pageable pageable);
    Page<DiscussionDto> getRootCommentsWithReplyCount(UUID lessonId, Pageable pageable);
    void deleteDiscussion(UUID id);
    
}
