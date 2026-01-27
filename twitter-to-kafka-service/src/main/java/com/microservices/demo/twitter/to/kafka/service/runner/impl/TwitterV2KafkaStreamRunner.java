package com.microservices.demo.twitter.to.kafka.service.runner.impl;

import com.microservices.demo.twitter.to.kafka.service.config.TwitterToKafkaServiceConfigData;
import com.microservices.demo.twitter.to.kafka.service.runner.StreamRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ParseException;
import org.json.JSONException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@ConditionalOnExpression("${twitter-to-kafka-service.enable-v2-tweets} && not ${twitter-to-kafka-service.enable-mock-tweets}")
@RequiredArgsConstructor
public class TwitterV2KafkaStreamRunner implements StreamRunner {

    private final TwitterToKafkaServiceConfigData configData;
    private final TwitterV2StreamHelper twitterV2StreamHelper;

    @Override
    public void start() {
        final var bearerToken  = configData.getTwitterV2BearerToken();
        if (null != bearerToken){
            try {
                twitterV2StreamHelper.setupRules(bearerToken, getRules());
                twitterV2StreamHelper.connectStream(bearerToken);
            } catch (IOException | URISyntaxException | JSONException | ParseException e) {
                log.error("Error streaming tweets!", e);
                throw new RuntimeException("Error streaming tweets!", e);
            }
        } else {
            log.error("There was a problem getting your bearer token. " +
                    "Please make sure you set the TWITTER_BEARER_TOKEN environment variable.");
            throw new RuntimeException("There was a problem getting your bearer token. " +
                    "Please make sure you set the TWITTER_BEARER_TOKEN environment variable.");
        }
    }

    private Map<String, String> getRules() {
        final var keywords = configData.getTwitterKeywords();
        final var rules = new HashMap<String, String>();
        for (final String keyword : keywords) {
            rules.put(keyword, "Keyword: " + keyword);
        }
        log.info("Created filter for Twitter stream for keywords: {}", keywords);
        return rules;
    }
}
