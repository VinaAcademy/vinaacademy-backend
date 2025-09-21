// FavoriteMapper.java
package com.vinaacademy.platform.feature.discussion.mapper;

import com.vinaacademy.platform.feature.discussion.dto.FavoriteDto;
import com.vinaacademy.platform.feature.discussion.entity.Favorite;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface FavoriteMapper {

    // Entity -> DTO
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "commentId", source = "comment.id")
    FavoriteDto toDto(Favorite entity);
 
    
}
