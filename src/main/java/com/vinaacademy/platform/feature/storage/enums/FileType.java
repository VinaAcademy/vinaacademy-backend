package com.vinaacademy.platform.feature.storage.enums;

import lombok.Getter;

@Getter
public enum FileType {
    VIDEO("video"),
    IMAGE("image"),
    DOCUMENT("document"),
    OTHER("other");

    private final String type;

    FileType(String type) {
        this.type = type;
    }

    public static FileType fromString(String type) {
        for (FileType fileType : FileType.values()) {
            if (fileType.type.equalsIgnoreCase(type)) {
                return fileType;
            }
        }
        return OTHER;
    }
}
