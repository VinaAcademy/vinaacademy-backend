package com.vinaacademy.platform.feature.video.service;

import java.util.UUID;

public interface VideoProgressCacheService {
    void saveProgress(UUID userId, UUID videoId, Long lastWatchedTime);

    Long getProgress(UUID userId, UUID videoId);
}
