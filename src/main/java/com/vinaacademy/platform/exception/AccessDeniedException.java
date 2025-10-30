package com.vinaacademy.platform.exception;

import lombok.Getter;

/**
 * Custom exception for access denied scenarios (HTTP 403). Supports internationalization through
 * message keys and arguments.
 */
@Getter
public class AccessDeniedException extends RuntimeException {

  private final String messageKey;
  private final Object[] messageArgs;

  public AccessDeniedException(String message) {
    super(message);
    this.messageKey = message;
    this.messageArgs = null;
  }

  public AccessDeniedException(String message, Throwable cause) {
    super(message, cause);
    this.messageKey = message;
    this.messageArgs = null;
  }

  /** Create exception with message key for i18n */
  public AccessDeniedException(String messageKey, Object... args) {
    super(messageKey);
    this.messageKey = messageKey;
    this.messageArgs = args;
  }

  /** Static factory method for message key */
  public static AccessDeniedException messageKey(String messageKey) {
    return new AccessDeniedException(messageKey);
  }

  /** Static factory method for message key with arguments */
  public static AccessDeniedException messageKey(String messageKey, Object... args) {
    return new AccessDeniedException(messageKey, args);
  }
}
