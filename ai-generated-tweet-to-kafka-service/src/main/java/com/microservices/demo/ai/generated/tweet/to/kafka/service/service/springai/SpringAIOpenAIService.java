package com.microservices.demo.ai.generated.tweet.to.kafka.service.service.springai;

import com.microservices.demo.ai.generated.tweet.to.kafka.service.config.AIGeneratedTweetToKafkaServiceConfigData;
import com.microservices.demo.ai.generated.tweet.to.kafka.service.exception.AIGeneratedTweetToKafkaServiceException;
import com.microservices.demo.ai.generated.tweet.to.kafka.service.service.AIService;
import com.microservices.demo.ai.generated.tweet.to.kafka.service.service.springai.model.TweetResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@ConditionalOnProperty(name = "ai-generated-tweet-to-kafka-service.ai-service", havingValue = "SpringAI-OpenAI")
public class SpringAIOpenAIService implements AIService {

    private final ChatClient chatClient;
    private final AIGeneratedTweetToKafkaServiceConfigData configData;

    @Value("classpath:/templates/tweet-prompt.st")
    private Resource tweetPrompt;

    public SpringAIOpenAIService(@Qualifier("openAIChatClient") ChatClient chatClient, AIGeneratedTweetToKafkaServiceConfigData configData) {
        this.chatClient = chatClient;
        this.configData = configData;
    }

    @Override
    public String generateTweet() throws AIGeneratedTweetToKafkaServiceException {
        final var converter = new BeanOutputConverter<>(TweetResponse.class);

        log.info("Converter format: {}", converter.getFormat());

        final var promptTemplate = new PromptTemplate(tweetPrompt);
        final var prompt = promptTemplate.create(Map.of(
                configData.getKeywordsPlaceholder()
                        .replace("{", "")
                        .replace("}", ""),
                String.join(",", configData.getStreamingDataKeywords()),
                "format",
                converter.getFormat()
        ));

        final var modelResult = chatClient.prompt(prompt)
                .call()
                .content();

        log.info("Model result: {}", modelResult);
        return modelResult;
    }
}
