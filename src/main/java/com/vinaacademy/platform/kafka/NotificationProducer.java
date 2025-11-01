package com.vinaacademy.platform.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import vn.vinaacademy.kafka.KafkaTopic;
import vn.vinaacademy.kafka.event.NotificationCreateEvent;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationProducer {
  private final KafkaTemplate<String, Object> kafkaTemplate;

  public void sendNotification(NotificationCreateEvent event) {
    kafkaTemplate
        .send(KafkaTopic.NOTIFICATION_TOPIC, event)
        .whenComplete(
            (res, ex) -> {
              if (ex != null) {
                log.error("Failed to send notification event to Kafka", ex);
              } else {
                log.info(
                    "Sent notification event to Kafka topic={} partition={} offset={}",
                    res.getRecordMetadata().topic(),
                    res.getRecordMetadata().partition(),
                    res.getRecordMetadata().offset());
              }
            });
  }
}
