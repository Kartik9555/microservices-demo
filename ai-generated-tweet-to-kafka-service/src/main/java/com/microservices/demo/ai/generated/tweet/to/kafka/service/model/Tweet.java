package com.microservices.demo.ai.generated.tweet.to.kafka.service.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.ZonedDateTime;

public record Tweet(
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "EEE MMM dd HH:mm:ss zzz yyyy")
        ZonedDateTime createdAt,
        Long id,
        String text,
        User user
) {
}
