package com.vinaacademy.platform.configuration.cache;

import lombok.Getter;

public enum CacheName {
    CATEGORY(CacheConstants.CATEGORY, 86400),
    CATEGORIES(CacheConstants.CATEGORIES, 86400),
    SHORT_TERM(CacheConstants.SHORT_TERM, 3600),
    LONG_TERM(CacheConstants.LONG_TERM, 86400);
    @Getter
    private final String value;
    @Getter
    private final int ttl;

    CacheName(String value, int ttl) {
        this.value = value;
        this.ttl = ttl;
    }
}
