package com.microservices.demo.ai.generated.tweet.to.kafka.service.service.openai;

import com.microservices.demo.ai.generated.tweet.to.kafka.service.config.AIGeneratedTweetToKafkaServiceConfigData;
import com.microservices.demo.ai.generated.tweet.to.kafka.service.exception.AIGeneratedTweetToKafkaServiceException;
import com.microservices.demo.ai.generated.tweet.to.kafka.service.service.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class OpenAIService implements AIService {

    private final AIGeneratedTweetToKafkaServiceConfigData configData;
    private final ObjectMapper objectMapper;

    @Override
    public String generateTweet() throws AIGeneratedTweetToKafkaServiceException {
        return "Open AI Generated Tweet Content";
    }
}
