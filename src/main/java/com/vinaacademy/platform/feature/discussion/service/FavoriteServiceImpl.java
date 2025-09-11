// FavoriteServiceImpl.java
package com.vinaacademy.platform.feature.discussion.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.vinaacademy.platform.exception.BadRequestException;
import com.vinaacademy.platform.exception.NotFoundException;
import com.vinaacademy.platform.feature.discussion.dto.FavoriteDto;
import com.vinaacademy.platform.feature.discussion.dto.request.FavoriteRequest;
import com.vinaacademy.platform.feature.discussion.entity.Discussion;
import com.vinaacademy.platform.feature.discussion.entity.Favorite;
import com.vinaacademy.platform.feature.discussion.mapper.FavoriteMapper;
import com.vinaacademy.platform.feature.discussion.repository.DiscussionRepository;
import com.vinaacademy.platform.feature.discussion.repository.FavoriteRepository;
import com.vinaacademy.platform.feature.user.auth.helpers.SecurityHelper;
import com.vinaacademy.platform.feature.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

	private final FavoriteRepository favoriteRepository;
	private final FavoriteMapper favoriteMapper;
	private final SecurityHelper securityHelper;
	private final DiscussionRepository discussionRepository;

	@Override
	public FavoriteDto createFavorite(FavoriteRequest request) {
		User user = securityHelper.getCurrentUser();
		
		Discussion discussion = discussionRepository.findById(request.getCommentId()).orElseThrow(
				() -> NotFoundException.message("Không tìm thấy thảo luận id " + request.getCommentId()));
		
		boolean isHasFavorite = favoriteRepository.existsByUserIdAndCommentId(user.getId(), request.getCommentId());
		if (isHasFavorite)
			throw BadRequestException.message("Thảo luận này bạn đã thích rồi!!");
		
		Favorite favoriteNew = Favorite.builder().user(user).comment(discussion).build();
		return favoriteMapper.toDto(favoriteRepository.save(favoriteNew));
	}

	@Override
	public void deleteFavorite(FavoriteRequest favoriteRequest) {
		User user = securityHelper.getCurrentUser();
		
		Favorite favorite = favoriteRepository.findByUserIdAndCommentId(user.getId(), favoriteRequest.getCommentId())
				.orElseThrow(() -> NotFoundException.message("Không tìm thấy lượt yêu thích này"));
		
		favoriteRepository.delete(favorite);
	}
}
