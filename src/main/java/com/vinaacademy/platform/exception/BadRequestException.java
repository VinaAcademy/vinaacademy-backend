package com.vinaacademy.platform.exception;

import lombok.*;

/**
 * Exception for bad request scenarios (HTTP 400).
 * Supports internationalization through message keys and arguments.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BadRequestException extends RuntimeException {
    private Integer code;
    private String message;
    private String messageKey;
    private Object[] messageArgs;

    /**
     * Create exception with simple message
     */
    public static BadRequestException message(String message) {
        return BadRequestException.builder()
                .code(400)
                .message(message)
                .messageKey(message)
                .build();
    }

    /**
     * Create exception with message key for i18n
     */
    public static BadRequestException messageKey(String messageKey) {
        return BadRequestException.builder()
                .code(400)
                .messageKey(messageKey)
                .build();
    }

    /**
     * Create exception with message key and arguments for i18n
     */
    public static BadRequestException messageKey(String messageKey, Object... args) {
        return BadRequestException.builder()
                .code(400)
                .messageKey(messageKey)
                .messageArgs(args)
                .build();
    }

    /**
     * Create exception with custom code and message key
     */
    public static BadRequestException of(Integer code, String messageKey) {
        return BadRequestException.builder()
                .code(code)
                .messageKey(messageKey)
                .build();
    }

    /**
     * Create exception with custom code, message key and arguments
     */
    public static BadRequestException of(Integer code, String messageKey, Object... args) {
        return BadRequestException.builder()
                .code(code)
                .messageKey(messageKey)
                .messageArgs(args)
                .build();
    }
}
