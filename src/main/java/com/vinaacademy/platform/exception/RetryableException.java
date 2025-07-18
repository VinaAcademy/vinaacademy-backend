package com.vinaacademy.platform.exception;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class RetryableException extends RuntimeException {
    private String code;
    private String message;

    public static RetryableException message(String message) {
        return RetryableException.builder().message(message).build();
    }
}