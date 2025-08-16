package com.vinaacademy.platform.feature.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception for resource not found scenarios (HTTP 404).
 * Supports internationalization through message keys and arguments.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
@Getter
public class ResourceNotFoundException extends RuntimeException {
    
    private final String messageKey;
    private final Object[] messageArgs;
    
    public ResourceNotFoundException(String message) {
        super(message);
        this.messageKey = message;
        this.messageArgs = null;
    }
    
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.messageKey = message;
        this.messageArgs = null;
    }

    /**
     * Create exception with message key for i18n
     */
    public ResourceNotFoundException(String messageKey, Object... args) {
        super(messageKey);
        this.messageKey = messageKey;
        this.messageArgs = args;
    }

    /**
     * Static factory method for message key
     */
    public static ResourceNotFoundException messageKey(String messageKey) {
        return new ResourceNotFoundException(messageKey);
    }

    /**
     * Static factory method for message key with arguments
     */
    public static ResourceNotFoundException messageKey(String messageKey, Object... args) {
        return new ResourceNotFoundException(messageKey, args);
    }
}
