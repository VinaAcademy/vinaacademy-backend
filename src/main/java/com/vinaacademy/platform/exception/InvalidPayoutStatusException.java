package com.vinaacademy.platform.exception;

public class InvalidPayoutStatusException extends RuntimeException {
    public InvalidPayoutStatusException(String message) {
        super(message);
    }
}
