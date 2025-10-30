package com.vinaacademy.platform.exception;

public class PayoutRequestNotFoundException extends RuntimeException {
    public PayoutRequestNotFoundException(String message) {
        super(message);
    }
}
