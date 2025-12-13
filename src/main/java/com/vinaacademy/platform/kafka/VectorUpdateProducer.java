package com.vinaacademy.platform.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.vinaacademy.platform.feature.course.constants.CourseConstants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.vinaacademy.kafka.KafkaTopic;
import vn.vinaacademy.kafka.event.CourseEmbeddedEvent;
import vn.vinaacademy.kafka.event.NotificationCreateEvent;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorUpdateProducer {
	private final KafkaTemplate<String, Object> kafkaTemplate;

	public void sendNotification(CourseEmbeddedEvent event) {
		kafkaTemplate.send(CourseConstants.VECTOR_TOPIC, event).whenComplete((res, ex) -> {
			if (ex != null) {
				log.error("Failed to send vector update event to Kafka", ex);
			} else {
				log.info("Sent vector update event to Kafka topic={} partition={} offset={}",
						res.getRecordMetadata().topic(), res.getRecordMetadata().partition(),
						res.getRecordMetadata().offset());
			}
		});
	}
}
