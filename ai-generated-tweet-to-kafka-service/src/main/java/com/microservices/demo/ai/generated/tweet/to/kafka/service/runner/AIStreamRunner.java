package com.microservices.demo.ai.generated.tweet.to.kafka.service.runner;

import com.microservices.demo.ai.generated.tweet.to.kafka.service.service.AIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ComponentScan(basePackages = "com.microservices.demo")
@EnableScheduling
@RequiredArgsConstructor
public class AIStreamRunner implements Runnable {

    private final AIService aiService;

    @Override
    public void run() {
        var generatedTweet = aiService.generateTweet();
        log.info("Generated Tweet: {}", generatedTweet);
    }
}
