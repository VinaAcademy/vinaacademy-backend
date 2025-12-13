// DiscussionServiceImpl.java
package com.vinaacademy.platform.feature.discussion.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.vinaacademy.platform.exception.BadRequestException;
import com.vinaacademy.platform.exception.NotFoundException;
import com.vinaacademy.platform.feature.discussion.dto.DiscussionDto;
import com.vinaacademy.platform.feature.discussion.dto.request.DiscussionRequest;
import com.vinaacademy.platform.feature.discussion.entity.Discussion;
import com.vinaacademy.platform.feature.discussion.mapper.DiscussionMapper;
import com.vinaacademy.platform.feature.discussion.repository.DiscussionRepository;
import com.vinaacademy.platform.feature.discussion.repository.FavoriteRepository;
import com.vinaacademy.platform.feature.discussion.repository.projection.DiscussionSummary;
import com.vinaacademy.platform.feature.lesson.entity.Lesson;
import com.vinaacademy.platform.feature.lesson.repository.LessonRepository;
import com.vinaacademy.platform.feature.user.auth.helpers.SecurityHelper;
import com.vinaacademy.platform.feature.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DiscussionServiceImpl implements DiscussionService {

	private final LessonRepository lessonRepository;
	private final SecurityHelper securityHelper;
	private final DiscussionRepository discussionRepository;
	private final FavoriteRepository favoriteRepository;

	@Override
	public DiscussionDto createDiscussion(DiscussionRequest request) {
		User user = securityHelper.getCurrentUser();
		Lesson lesson = lessonRepository.findById(request.getLessonId())
				.orElseThrow(() -> NotFoundException.message("Id của bài học này không tồn tại"));
		Discussion parentComment = null;
		if (request.getParentCommentId() != null)
			parentComment = discussionRepository.findById(request.getParentCommentId())
					.orElseThrow(() -> NotFoundException.message("Id của thảo luận cha này không tìm thấy"));

		Discussion discussion = Discussion.builder().lesson(lesson).user(user).comment(request.getComment())
				.parentComment(parentComment).build();
		Discussion saveDiscussion = discussionRepository.save(discussion);
		DiscussionDto discussionDto = DiscussionMapper.INSTANCE.toDto(saveDiscussion);
		discussionDto.setAvatarUrl(user.getAvatarUrl());
		discussionDto.setUserFullName(user.getFullName());
		return discussionDto;
	}

	@Override
	public Page<DiscussionDto> getRepliesWithReplyCount(UUID parentId, Pageable pageable) {
		UUID currentUserId = securityHelper.getCurrentUser().getId();
		Page<DiscussionSummary> pageResult = discussionRepository.findReplySummaries(parentId, currentUserId, pageable);

		return pageResult.map(p -> DiscussionDto.builder()
				.id(p.getId())
				.comment(p.getComment())
				.lessonId(p.getLessonId())
				.userId(p.getUserId())
				.replyCount(p.getReplyCount())
				.favoriteCount(p.getFavoriteCount())
				.userFullName(p.getUserFullName())
				.avatarUrl(p.getAvatarUrl())
				.createdDate(p.getCreatedDate())
				.likedByCurrentUser(Boolean.TRUE.equals(p.getLikedByCurrentUser()))
				.parentCommentId(parentId)
				.build());
	}

	@Override
	public Page<DiscussionDto> getRootCommentsWithReplyCount(UUID lessonId, Pageable pageable) {
		UUID currentUserId = securityHelper.getCurrentUser().getId();
		Page<DiscussionSummary> pageResult = discussionRepository.findRootCommentSummaries(lessonId, currentUserId, pageable);

		return pageResult.map(p -> DiscussionDto.builder()
				.id(p.getId())
				.comment(p.getComment())
				.lessonId(p.getLessonId())
				.userId(p.getUserId())
				.replyCount(p.getReplyCount())
				.favoriteCount(p.getFavoriteCount())
				.userFullName(p.getUserFullName())
				.avatarUrl(p.getAvatarUrl())
				.createdDate(p.getCreatedDate())
				.likedByCurrentUser(Boolean.TRUE.equals(p.getLikedByCurrentUser()))
				.parentCommentId(null)
				.build());
	}

	@Override
	public void deleteDiscussion(UUID id) {
		User user = securityHelper.getCurrentUser();
		Discussion discussion = discussionRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Thảo luận này không tồn tại"));
		if (!discussion.getUser().equals(user))
			throw BadRequestException.message("Bạn không có quyền xóa thảo luận này");
		discussionRepository.deleteById(id);
	}
}
