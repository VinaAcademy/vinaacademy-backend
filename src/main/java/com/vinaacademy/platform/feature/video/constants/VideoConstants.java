package com.vinaacademy.platform.feature.video.constants;

public class VideoConstants {
    public static final String HLS_MASTER_PLAYLIST = "master.m3u8";
    public static final String HLS_SEGMENT_PATTERN = "segment_%03d.ts";
    public static final int HLS_SEGMENT_DURATION = 4;
    public static final String DEFAULT_THUMBNAIL_TIMESTAMP = "00:00:01";
    
    public static class ContentTypes {
        public static final String M3U8 = "application/x-mpegURL";
        public static final String TS = "video/MP2T";
    }
    
    public static class CacheKeys {
        public static final String VIDEO_PROGRESS = "video:progress:";
        public static final long PROGRESS_EXPIRE_TIME = 60 * 60 * 24; // 1 day
    }
}