package com.vinaacademy.platform.feature.discussion.controller;

import com.vinaacademy.platform.feature.common.response.ApiResponse;
import com.vinaacademy.platform.feature.discussion.dto.FavoriteDto;
import com.vinaacademy.platform.feature.discussion.dto.request.FavoriteRequest;
import com.vinaacademy.platform.feature.discussion.service.FavoriteService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/favorites")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping
    @Operation(summary = "Tạo lượt thích đối với 1 comment")
    public ApiResponse<FavoriteDto> createFavorite(@RequestBody FavoriteRequest request) {
        FavoriteDto favorite = favoriteService.createFavorite(request);
        return ApiResponse.success(favorite);
    }

    @DeleteMapping
    @Operation(summary = "Xóa lượt thích đối với 1 comment mà mình đã thích")
    public ApiResponse<String> deleteFavorite(@RequestBody FavoriteRequest request) {
        favoriteService.deleteFavorite(request);
        return ApiResponse.success("Xóa thành công lượt thích với id comment "+request.getCommentId());
    }
}
