package com.microservices.demo.ai.generated.tweet.to.kafka.service.service.springai.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.ZonedDateTime;

public record TweetResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "EEE MMM dd HH:mm:ss zzz yyy")
        ZonedDateTime createdAt,
        Long id,
        String text,
        User user
) {
}
