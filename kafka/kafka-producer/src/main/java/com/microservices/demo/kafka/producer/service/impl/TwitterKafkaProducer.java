package com.microservices.demo.kafka.producer.service.impl;

import com.microservices.demo.kafka.avro.model.TwitterAvroModel;
import com.microservices.demo.kafka.producer.service.KafkaProducer;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwitterKafkaProducer implements KafkaProducer<Long, TwitterAvroModel> {

    private final KafkaTemplate<Long,TwitterAvroModel> kafkaTemplate;

    @Override
    public void send(String topic, Long key, TwitterAvroModel message) {
        log.info("Sending message: {} to topic: {}", message, topic);
        CompletableFuture<SendResult<Long, TwitterAvroModel>> future = kafkaTemplate.send(topic, key, message);
        future.handle((result, throwable) -> {
            if (throwable != null) {
                log.error("Error while sending message: {} to topic: {}", message.toString(), topic, throwable);
            } else {
                RecordMetadata recordMetadata = result.getRecordMetadata();
                log.info("Received new metadata. Topic {}, Partition {}, Offset {}, Timestamp {}, at time {}",
                        recordMetadata.topic(),
                        recordMetadata.partition(),
                        recordMetadata.offset(),
                        recordMetadata.timestamp(),
                        System.currentTimeMillis()
                );
            }
            return null;
        });
    }

    @PreDestroy
    public void close() {
        if (kafkaTemplate != null) {
            log.info("Closing kafka producer");
            kafkaTemplate.destroy();
        }
    }
}
