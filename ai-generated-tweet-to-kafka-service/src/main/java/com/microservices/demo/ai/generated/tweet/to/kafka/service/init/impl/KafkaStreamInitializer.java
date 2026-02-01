package com.microservices.demo.ai.generated.tweet.to.kafka.service.init.impl;

import com.microservices.demo.ai.generated.tweet.to.kafka.service.init.StreamInitializer;
import com.microservices.demo.config.KafkaConfigData;
import com.microservices.demo.kafka.admin.client.KafkaAdminClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaStreamInitializer implements StreamInitializer {

    private final KafkaConfigData kafkaConfigData;
    private final KafkaAdminClient kafkaAdminClient;

    @Override
    public boolean init() {
        try {
            kafkaAdminClient.createTopic();
            kafkaAdminClient.checkSchemaRegistry();
            log.info("Topic with name {} is ready for operations!", kafkaConfigData.getTopicNamesToCreate().toArray());
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }
}
