package com.vinaacademy.platform.exception;

public class DuplicateVnpayTransactionException extends RuntimeException{
	public DuplicateVnpayTransactionException(String message) {
        super(message);
    }
}
