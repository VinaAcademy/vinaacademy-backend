package com.vinaacademy.platform.feature.log.dto;

import com.vinaacademy.platform.feature.common.dto.BaseDto;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LogDto extends BaseDto {
    private Long id;
    private String name;
    private String event;
    private String description;
    private String username;
    private String oldData;
    private String newData;
    private String ipAddress;
    private String userAgent;
}
