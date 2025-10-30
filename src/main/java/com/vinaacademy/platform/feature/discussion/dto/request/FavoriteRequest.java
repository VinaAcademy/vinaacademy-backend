// FavoriteRequest.java
package com.vinaacademy.platform.feature.discussion.dto.request;

import lombok.*;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteRequest {
    private UUID commentId;
}
