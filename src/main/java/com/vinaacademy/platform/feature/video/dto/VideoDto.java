package com.vinaacademy.platform.feature.video.dto;

import com.vinaacademy.platform.feature.common.dto.BaseDto;
import com.vinaacademy.platform.feature.video.enums.VideoStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoDto extends BaseDto {
    @EqualsAndHashCode.Include
    private String videoId;
    private String thumbnailUrl;
    private String originalFilename;
    private VideoStatus status;
    private double duration;
}
