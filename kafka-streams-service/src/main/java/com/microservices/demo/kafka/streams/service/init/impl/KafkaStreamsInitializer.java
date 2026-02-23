package com.microservices.demo.kafka.streams.service.init.impl;

import com.microservices.demo.config.KafkaConfigData;
import com.microservices.demo.kafka.admin.client.KafkaAdminClient;
import com.microservices.demo.kafka.streams.service.init.StreamsInitializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaStreamsInitializer implements StreamsInitializer {

    private final KafkaConfigData kafkaConfigData;
    private final KafkaAdminClient kafkaAdminClient;

    @Override
    public boolean init() {
        try {
            kafkaAdminClient.checkTopicsCreated();
            kafkaAdminClient.checkSchemaRegistry();
            log.info("Topics with name {} is ready for operations", kafkaConfigData.getTopicNamesToCreate().toArray());
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }
}