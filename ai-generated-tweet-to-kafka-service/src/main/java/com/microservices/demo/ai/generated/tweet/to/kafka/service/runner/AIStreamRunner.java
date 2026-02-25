package com.microservices.demo.ai.generated.tweet.to.kafka.service.runner;

import com.microservices.demo.ai.generated.tweet.to.kafka.service.service.AIService;
import com.microservices.demo.ai.generated.tweet.to.kafka.service.transformer.TwitterStatusToAvroTransformer;
import com.microservices.demo.config.KafkaConfigData;
import com.microservices.demo.kafka.avro.model.TwitterAvroModel;
import com.microservices.demo.kafka.producer.service.KafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.springframework.util.IdGenerator;

@Slf4j
@Component
@ComponentScan(basePackages = "com.microservices.demo")
@EnableScheduling
@RequiredArgsConstructor
public class AIStreamRunner implements Runnable {

    private final AIService aiService;
    private final KafkaConfigData kafkaConfigData;
    private final KafkaProducer<Long, TwitterAvroModel> kafkaProducer;
    private final TwitterStatusToAvroTransformer twitterStatusToAvroTransformer;
    private final IdGenerator idGenerator;

    @Override
    public void run() {
        final var generatedTweet = aiService.generateTweet();
        log.info("Generated Tweet: {}", generatedTweet);
        final var id = idGenerator.generateId().getMostSignificantBits();
        final var twitterAvroModel = twitterStatusToAvroTransformer.getTwitterAvroModelFromStatus(generatedTweet, id);
        kafkaProducer.send(kafkaConfigData.getTopicName(), twitterAvroModel.getUserId(), twitterAvroModel);
    }
}
