// FavoriteService.java
package com.vinaacademy.platform.feature.discussion.service;

import com.vinaacademy.platform.feature.discussion.dto.FavoriteDto;
import com.vinaacademy.platform.feature.discussion.dto.request.FavoriteRequest;

public interface FavoriteService {
    FavoriteDto createFavorite(FavoriteRequest request);
    void deleteFavorite(FavoriteRequest deleteRequest);
}
