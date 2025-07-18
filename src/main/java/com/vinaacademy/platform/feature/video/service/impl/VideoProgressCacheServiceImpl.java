package com.vinaacademy.platform.feature.video.service.impl;

import com.vinaacademy.platform.feature.video.service.VideoProgressCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class VideoProgressCacheServiceImpl implements VideoProgressCacheService {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String VIDEO_PROGRESS_CACHE_KEY = "video:progress:";
    private static final long VIDEO_PROGRESS_CACHE_EXPIRE_TIME = 60 * 60 * 24; // 1 day

    private String getCacheKey(UUID userId, UUID videoId) {
        return String.format(VIDEO_PROGRESS_CACHE_KEY + "%s:%s", userId, videoId);
    }

    @Override
    public void saveProgress(UUID userId, UUID videoId, Long lastWatchedTime) {
        String key = getCacheKey(userId, videoId);
        redisTemplate.opsForValue().set(key, lastWatchedTime);
        redisTemplate.expire(key, VIDEO_PROGRESS_CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
    }

    @Override
    public Long getProgress(UUID userId, UUID videoId) {
        String key = getCacheKey(userId, videoId);
        Object value = redisTemplate.opsForValue().get(key);
        if (value instanceof Number) {
            return Long.valueOf(value.toString());
        }
        return 0L;
    }
}
