package com.microservices.demo.ai.generated.tweet.to.kafka.service;

import com.microservices.demo.ai.generated.tweet.to.kafka.service.config.AIGeneratedTweetToKafkaServiceConfigData;
import com.microservices.demo.ai.generated.tweet.to.kafka.service.init.StreamInitializer;
import com.microservices.demo.ai.generated.tweet.to.kafka.service.runner.AIStreamRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.TaskScheduler;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class AIGeneratedTweetToKafkaServiceApplication implements CommandLineRunner {

    private final AIGeneratedTweetToKafkaServiceConfigData configData;
    private final StreamInitializer streamInitializer;
    private final AIStreamRunner aiStreamRunner;
    private final TaskScheduler taskScheduler;

    public static void main(String[] args) {
        SpringApplication.run(AIGeneratedTweetToKafkaServiceApplication.class, args);
    }

    @Override
    public void run(String @NonNull ... args) throws Exception {
        log.info("Application is starting...");
        var initResult = streamInitializer.init();
        if(initResult) {
            log.info("Starting AI Stream Runner with fixed rate {} seconds", configData.getSchedulerDurationSec());
            taskScheduler.scheduleAtFixedRate(aiStreamRunner, Duration.of(configData.getSchedulerDurationSec(), ChronoUnit.SECONDS));
        } else {
            log.error("Stream initializer failed to initialize the streams! Not starting the AI Stream Runner!");
        }
    }
}
