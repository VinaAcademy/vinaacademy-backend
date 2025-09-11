// DiscussionMapper.java
package com.vinaacademy.platform.feature.discussion.mapper;

import com.vinaacademy.platform.feature.discussion.dto.DiscussionDto;
import com.vinaacademy.platform.feature.discussion.entity.Discussion;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {FavoriteMapper.class})
public interface DiscussionMapper {
	 
	DiscussionMapper INSTANCE = Mappers.getMapper(DiscussionMapper.class);
	
	@Mapping(target = "parentCommentId", source = "parentComment.id")
	@Mapping(target = "lessonId", source = "lesson.id" )
	@Mapping(target = "userId", source = "user.id" )
	@Mapping(target = "replyCount", ignore = true )
    DiscussionDto toDto(Discussion entity);   
	 
}
