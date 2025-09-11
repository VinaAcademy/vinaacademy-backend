// FavoriteDto.java
package com.vinaacademy.platform.feature.discussion.dto;

import lombok.*;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteDto {
    private UUID id;
    private UUID userId;
    private UUID commentId;
}
