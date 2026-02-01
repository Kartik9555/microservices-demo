package com.microservices.demo.ai.generated.tweet.to.kafka.service.transformer;

import com.microservices.demo.ai.generated.tweet.to.kafka.service.model.Tweet;
import com.microservices.demo.kafka.avro.model.TwitterAvroModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class TwitterStatusToAvroTransformer {

    private final ObjectMapper objectMapper;

    public TwitterAvroModel getTwitterAvroModelFromStatus(String status) {
        final var tweet = objectMapper.readValue(status, Tweet.class);
        return TwitterAvroModel.newBuilder()
                .setId(tweet.id())
                .setText(tweet.text())
                .setCreatedAt(tweet.createdAt().toEpochSecond())
                .setUserId(tweet.user().id())
                .build();
    }
}
