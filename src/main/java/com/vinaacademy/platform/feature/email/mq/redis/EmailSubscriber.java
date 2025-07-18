package com.vinaacademy.platform.feature.email.mq.redis;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class EmailSubscriber implements MessageListener {
    @Autowired
    private EmailConsumer emailConsumer;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        // Khi nhận được thông báo từ Pub/Sub, xử lý hàng đợi
        log.info("onMessage Email: ", message);
        emailConsumer.processEmailQueue();
    }
}
