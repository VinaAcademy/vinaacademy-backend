package com.vinaacademy.platform.feature.email.service;

import com.vinaacademy.platform.feature.user.entity.User;

public interface EmailService {

    void sendEmail(String toEmail, String subject, String body, boolean enableHtml);

    void sendEmailWithoutMQ(String toEmail, String subject, String body, boolean enableHtml);

    void sendEmailMQ(String toEmail, String subject, String body, boolean enableHtml);

    void sendVerificationEmail(String email, String token);

    void sendPasswordResetEmail(User user, String token);

    void sendWelcomeEmail(User user);
    
    void sendNotificationEmail(String email, String title, String message, String actionUrl, String actionText);
    
    void sendPaymentSuccessEmail(User user, String orderId, String amount, String orderTime, String courseUrl);
    
    void sendPaymentFailedEmail(User user, String orderId, String errorMessage, String orderTime, String retryUrl);
}