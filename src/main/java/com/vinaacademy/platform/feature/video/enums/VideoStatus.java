package com.vinaacademy.platform.feature.video.enums;


import lombok.Getter;

@Getter
public enum VideoStatus {
    NO_VIDEO("Chưa có video", false),
    PROCESSING("Đang xử lý", false),
    READY("Đã sẵn sàng", true),
    ERROR("Đã có lỗi xảy ra", false),
    ;
    private final String displayName;
    private final boolean accessible;

    VideoStatus(String displayName, boolean accessible) {
        this.displayName = displayName;
        this.accessible = accessible;
    }

    public boolean canProcess() {
        return this != PROCESSING;
    }

}
