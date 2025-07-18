package com.vinaacademy.platform.feature.email.mq.redis;

import com.vinaacademy.platform.feature.email.dto.EmailMessage;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import static com.vinaacademy.platform.feature.email.mq.redis.EmailQueueConstant.EMAIL_CHANNEL;
import static com.vinaacademy.platform.feature.email.mq.redis.EmailQueueConstant.EMAIL_QUEUE;

@Service
@Log4j2
public class EmailProducer {

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    public void enqueueEmail(EmailMessage emailMessage) {
        if (redisTemplate == null) {
            log.error("redisTemplate is null");
            return;
        }
        // Thêm email vào hàng đợi
        redisTemplate.opsForList().leftPush(EMAIL_QUEUE, emailMessage);
        log.info("Email enqueued to: " + emailMessage.getTo());

        // Gửi thông báo qua Pub/Sub
        redisTemplate.convertAndSend(EMAIL_CHANNEL, emailMessage);
    }
}
