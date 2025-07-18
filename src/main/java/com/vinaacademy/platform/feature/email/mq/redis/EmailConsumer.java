package com.vinaacademy.platform.feature.email.mq.redis;

import com.vinaacademy.platform.feature.email.dto.EmailMessage;
import com.vinaacademy.platform.feature.email.service.EmailService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.vinaacademy.platform.feature.email.mq.redis.EmailQueueConstant.EMAIL_QUEUE;

@Service
@Log4j2
public class EmailConsumer {

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private EmailService emailService;

    public void processEmailQueue() {
        log.info("start send email.... ");
        EmailMessage emailMessage = (EmailMessage) redisTemplate.opsForList().rightPop(EMAIL_QUEUE, 5, TimeUnit.SECONDS);
        if (emailMessage != null) {
            emailService.sendEmailWithoutMQ(emailMessage.getTo(), emailMessage.getSubject(), emailMessage.getBody(),
                    emailMessage.isEnableHtml());
            log.info("Processed email to: " + emailMessage.getTo());
        }
    }
}
