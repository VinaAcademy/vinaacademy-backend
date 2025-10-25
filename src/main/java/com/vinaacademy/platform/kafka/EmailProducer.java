package com.vinaacademy.platform.kafka;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import vn.vinaacademy.kafka.KafkaTopic;
import vn.vinaacademy.kafka.event.GenericEmailEvent;

@Component
@Slf4j
public class EmailProducer {
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendVerificationEmail(String email, String token) {
        sendToken(email, token, GenericEmailEvent.EmailEventType.VERIFICATION);
    }

    public void sendPasswordResetEmail(String email, String token) {
        sendToken(email, token, GenericEmailEvent.EmailEventType.PASSWORD_RESET);
    }

    public void sendWelcomeEmail(String email, String fullName) {
        log.info("Sending welcome email to: {}, fullName: {}", email, fullName);
        GenericEmailEvent message = GenericEmailEvent.builder()
                .type(GenericEmailEvent.EmailEventType.WELCOME)
                .data(Map.of(
                        "email", email,
                        "fullName", fullName
                ))
                .build();
        kafkaTemplate.send(KafkaTopic.EMAIL_TOPIC, message)
                .whenCompleteAsync((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send welcome email to {}: {}", email, ex.getMessage());
                    } else {
                        log.info("Welcome email sent successfully to {}", email);
                    }
                });
    }

    private void sendToken(String email, String token, GenericEmailEvent.EmailEventType eventType) {
        log.info("Sending {} email to: {}", eventType, email);
        GenericEmailEvent message = GenericEmailEvent.builder()
                .type(eventType)
                .data(Map.of(
                        "email", email,
                        "token", token
                ))
                .build();
        kafkaTemplate.send(KafkaTopic.EMAIL_TOPIC, message)
                .whenCompleteAsync((result, ex) -> {
                  if (ex != null) {
                        log.error("Failed to send {} email to {}: {}", eventType, email, ex.getMessage());
                    } else {
                        log.info("{} email sent successfully to {}", eventType, email);
                    }
                });
    }
}