package com.vinaacademy.platform.feature.email.enums;

import lombok.Getter;

@Getter
public enum EmailTemplate {
    VERIFY_ACCOUNT("email/verify-account"),
    RESET_PASSWORD("email/reset-password"),
    WELCOME("email/welcome"),
    NOTIFICATION("email/notification"),
    PAYMENT_SUCCESS("email/payment-success"),
    PAYMENT_FAILED("email/payment-failed");

    private final String templateName;

    EmailTemplate(String templateName) {
        this.templateName = templateName;
    }
}
