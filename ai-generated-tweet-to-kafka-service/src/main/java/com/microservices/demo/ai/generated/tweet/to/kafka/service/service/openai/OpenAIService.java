package com.microservices.demo.ai.generated.tweet.to.kafka.service.service.openai;

import com.microservices.demo.ai.generated.tweet.to.kafka.service.config.AIGeneratedTweetToKafkaServiceConfigData;
import com.microservices.demo.ai.generated.tweet.to.kafka.service.exception.AIGeneratedTweetToKafkaServiceException;
import com.microservices.demo.ai.generated.tweet.to.kafka.service.service.AIService;
import com.microservices.demo.ai.generated.tweet.to.kafka.service.service.openai.model.OpenAIRequest;
import com.microservices.demo.ai.generated.tweet.to.kafka.service.service.openai.model.OpenAIResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai-generated-tweet-to-kafka-service.ai-service", havingValue = "OpenAI")
public class OpenAIService implements AIService {

    private final AIGeneratedTweetToKafkaServiceConfigData configData;
    private final ObjectMapper objectMapper;

    @Override
    public String generateTweet() throws AIGeneratedTweetToKafkaServiceException {
        log.info("Generating tweet using OpenAIService");
        var prompt = configData.getPrompt().replace(configData.getKeywordsPlaceholder(), String.join("," , configData.getStreamingDataKeywords()));
        try(CloseableHttpClient httpClient = HttpClients.createDefault()) {
            var request = getRequest(prompt);
            var response = httpClient.execute(request, res -> EntityUtils.toString(res.getEntity()));
            return parseResponse(response);
        } catch (IOException e) {
            throw new AIGeneratedTweetToKafkaServiceException("Failed to generate tweet from OpenAI", e);
        }
    }

    private HttpPost getRequest(String prompt) throws JacksonException {
        var request = new HttpPost(configData.getOpenAI().getUrl());
        request.setHeader(HttpHeaders.CONTENT_TYPE, configData.getOpenAI().getContentType());
        request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + configData.getOpenAI().getApiKey());
        var openAIRequest = OpenAIRequest.builder()
                .model(configData.getOpenAI().getModel())
                .messages(configData.getOpenAI()
                        .getMessages()
                            .stream()
                            .map(message -> OpenAIRequest.Message.builder()
                                .role(message.getRole())
                                .content(List.of(OpenAIRequest.Content.builder()
                                        .type(message.getContent().getFirst().getType())
                                        .text(prompt)
                                        .build())
                                )
                                .build()
                            )
                        .toList()
                )
                .max_completion_tokens(configData.getOpenAI().getMaxCompletionTokens())
                .temperature(configData.getOpenAI().getTemperature())
                .build();
        request.setEntity(new StringEntity(objectMapper.writeValueAsString(openAIRequest)));
        return request;
    }

    private String parseResponse(String response) throws JacksonException {
        var openAIResponse = objectMapper.readValue(response, OpenAIResponse.class);
        return openAIResponse.getChoices().getFirst().getMessage().getContent();
    }
}
