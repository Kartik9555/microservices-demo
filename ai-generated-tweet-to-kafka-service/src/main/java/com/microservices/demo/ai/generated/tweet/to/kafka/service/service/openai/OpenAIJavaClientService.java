package com.microservices.demo.ai.generated.tweet.to.kafka.service.service.openai;

import com.microservices.demo.config.AIGeneratedTweetToKafkaServiceConfigData;
import com.microservices.demo.ai.generated.tweet.to.kafka.service.exception.AIGeneratedTweetToKafkaServiceException;
import com.microservices.demo.ai.generated.tweet.to.kafka.service.service.AIService;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai-generated-tweet-to-kafka-service.ai-service", havingValue = "OpenAI-JavaClient")
public class OpenAIJavaClientService implements AIService {

    private final AIGeneratedTweetToKafkaServiceConfigData configData;

    @Override
    public String generateTweet() throws AIGeneratedTweetToKafkaServiceException {
        var prompt = configData.getPrompt().replace(configData.getKeywordsPlaceholder(), String.join("," , configData.getStreamingDataKeywords()));
        return getPromptResponse(prompt);
    }

    private String getPromptResponse(final String prompt) {
        log.info("Generating tweet using OpenAIJavaClientService");
        var client = OpenAIOkHttpClient.fromEnv();
        var createParams = ChatCompletionCreateParams.builder()
                .model(ChatModel.of(configData.getOpenAI().getModel()))
                .addDeveloperMessage("You're helping me to create a tweet content based on the given format and keywords.")
                .maxCompletionTokens(configData.getOpenAI().getMaxCompletionTokens())
                .temperature(configData.getOpenAI().getTemperature())
                .addUserMessage(prompt);

        var messages = client.chat()
                .completions()
                .create(createParams.build())
                .choices()
                .stream()
                .map(ChatCompletion.Choice::message)
                .toList();

        return messages.getFirst().content().get();
    }
}
