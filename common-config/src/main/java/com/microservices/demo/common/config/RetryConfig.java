package com.microservices.demo.common.config;

import com.microservices.demo.config.RetryConfigData;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
@RequiredArgsConstructor
public class RetryConfig {

    private final RetryConfigData configData;

    @Bean
    public RetryTemplate retryTemplate() {
        final var retryTemplate = new RetryTemplate();

        final var backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(configData.getInitialIntervalMs());
        backOffPolicy.setMaxInterval(configData.getMaxIntervalMs());
        backOffPolicy.setMultiplier(configData.getMultiplier());
        retryTemplate.setBackOffPolicy(backOffPolicy);

        final var simpleRetryPolicy = new SimpleRetryPolicy();
        simpleRetryPolicy.setMaxAttempts(configData.getMaxAttempts());
        retryTemplate.setRetryPolicy(simpleRetryPolicy);

        return retryTemplate;
    }

}
