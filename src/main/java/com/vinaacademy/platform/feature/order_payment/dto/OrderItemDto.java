package com.vinaacademy.platform.feature.order_payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDto {
	private Long id;
    private UUID order_id;
    private String course_name;
    private String url_image;
    private UUID course_id;
    private BigDecimal price;
}
