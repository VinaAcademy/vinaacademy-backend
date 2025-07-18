package com.vinaacademy.platform.feature.video.controller;

import com.vinaacademy.platform.feature.user.auth.helpers.SecurityHelper;
import com.vinaacademy.platform.feature.video.service.VideoProgressCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/video-progress")
@RequiredArgsConstructor
public class VideoProgressController {

    private final VideoProgressCacheService videoProgressCacheService;
    private final SecurityHelper securityHelper;

    @PostMapping("/{videoId}")
    public ResponseEntity<?> saveProgress(@PathVariable UUID videoId,
                                          @RequestParam Long lastWatchedTime) {
        UUID userId = securityHelper.getCurrentUser().getId();
        videoProgressCacheService.saveProgress(userId, videoId, lastWatchedTime);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{videoId}")
    public ResponseEntity<Long> getProgress(@PathVariable UUID videoId) {
        UUID userId = securityHelper.getCurrentUser().getId();
        Long progress = videoProgressCacheService.getProgress(userId, videoId);
        return ResponseEntity.ok(progress);
    }
}
