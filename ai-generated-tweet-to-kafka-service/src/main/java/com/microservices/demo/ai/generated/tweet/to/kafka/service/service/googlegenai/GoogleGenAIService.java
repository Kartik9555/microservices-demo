package com.microservices.demo.ai.generated.tweet.to.kafka.service.service.googlegenai;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.microservices.demo.ai.generated.tweet.to.kafka.service.config.AIGeneratedTweetToKafkaServiceConfigData;
import com.microservices.demo.ai.generated.tweet.to.kafka.service.exception.AIGeneratedTweetToKafkaServiceException;
import com.microservices.demo.ai.generated.tweet.to.kafka.service.service.AIService;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "ai-generated-tweet-to-kafka-service.ai-service", havingValue = "GoogleGenAI")
public class GoogleGenAIService implements AIService {

    private final AIGeneratedTweetToKafkaServiceConfigData configData;
    private final Client googleGenAIClient;

    public GoogleGenAIService(AIGeneratedTweetToKafkaServiceConfigData configData) {
        this.configData = configData;
        this.googleGenAIClient = Client.builder()
                .project(configData.getGoogleGenAI().getProjectId())
                .location(configData.getGoogleGenAI().getLocation())
                .vertexAI(true) // Use Vertex AI backend
                .build();
    }

    @PreDestroy
    public void close() {
        if(this.googleGenAIClient != null) {
            googleGenAIClient.close();
        }
    }

    @Override
    public String generateTweet() throws AIGeneratedTweetToKafkaServiceException {
        log.info("Generating tweet using GoogleGenAIService");
        final var prompt = configData.getPrompt().replace(configData.getKeywordsPlaceholder(), String.join("," , configData.getStreamingDataKeywords()));
        return getPromptResponse(prompt);
    }

    private String getPromptResponse(final String prompt) {
        final var config = GenerateContentConfig.builder()
                .maxOutputTokens(configData.getGoogleGenAI().getMaxOutputTokens())
                .temperature(configData.getGoogleGenAI().getTemperature())
                .candidateCount(configData.getGoogleGenAI().getCandidateCount())
                .build();
        final var modelName = configData.getGoogleGenAI().getModelName();
        final var response = googleGenAIClient.models.generateContent(modelName, prompt, config);
        return response.text();
    }
}
