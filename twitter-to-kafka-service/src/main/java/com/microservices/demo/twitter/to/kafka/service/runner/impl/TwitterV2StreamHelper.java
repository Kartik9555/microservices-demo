package com.microservices.demo.twitter.to.kafka.service.runner.impl;

import com.microservices.demo.twitter.to.kafka.service.config.TwitterToKafkaServiceConfigData;
import com.microservices.demo.twitter.to.kafka.service.listener.TwitterKafkaStatusListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.net.URIBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Component
@ConditionalOnExpression("${twitter-to-kafka-service.enable-v2-tweets} && not ${twitter-to-kafka-service.enable-mock-tweets}")
@RequiredArgsConstructor
public class TwitterV2StreamHelper {

    private final TwitterToKafkaServiceConfigData configData;
    private final TwitterKafkaStatusListener twitterKafkaStatusListener;

    private static final String TWEET_AS_RAW_JSON = "{" +
            "\"created_at\": \"{0}\"," +
            "\"id\": \"{1}\"," +
            "\"text\":\"{2}\"," +
            "\"user\":{\"id\":\"{3}\"}," +
            "}";

    private static final String TWITTER_STATUS_DATE_FORMAT = "EEE MMM dd HH:mm:ss zzz yyy";

    /*
     * This method calls the filtered stream endpoint and streams Tweets from it
     * */
    void connectStream(final String bearerToken) throws IOException, URISyntaxException, JSONException {

        final var httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec("STANDARD")
                        .build()
                )
                .build();

        final var uriBuilder = new URIBuilder(configData.getTwitterV2BaseUrl());

        final var httpGet = new HttpGet(uriBuilder.build());
        httpGet.setHeader("Authorization", String.format("Bearer %s", bearerToken));

        final var response = httpClient.execute(httpGet);
        final var entity = response.getEntity();
        if (null != entity) {
            final var reader = new BufferedReader(new InputStreamReader((entity.getContent())));
            String line = reader.readLine();
            while (line != null) {
                System.out.println(line);
                line = reader.readLine();
                if(!line.isEmpty()){
                    final var tweet = getFormattedTweet(line);
                    Status status = null;
                    try {
                        status = TwitterObjectFactory.createStatus(tweet);
                    } catch (TwitterException e) {
                        log.error("Could not create status for text: {}", tweet, e);
                    }
                    if(status != null){
                        twitterKafkaStatusListener.onStatus(status);
                    }
                }
            }
        }
    }

    /*
     * Helper method to setup rules before streaming data
     * */
    void setupRules(final String bearerToken, final Map<String, String> rules) throws IOException, URISyntaxException, JSONException, ParseException {
        final var existingRules = getRules(bearerToken);
        if (!existingRules.isEmpty()) {
            deleteRules(bearerToken, existingRules);
        }
        createRules(bearerToken, rules);
        log.info("Created rules for twitter stream {}", rules.keySet().toArray());
    }

    /*
     * Helper method to create rules for filtering
     * */
    void createRules(final String bearerToken, final Map<String, String> rules) throws URISyntaxException, IOException, ParseException {
        final var httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec("STANDARD").build())
                .build();

        final var uriBuilder = new URIBuilder(configData.getTwitterV2RulesBaseUrl());

        final var httpPost = new HttpPost(uriBuilder.build());
        httpPost.setHeader("Authorization", String.format("Bearer %s", bearerToken));
        httpPost.setHeader("content-type", "application/json");
        final var body = new StringEntity(getFormattedString("{\"add\": [%s]}", rules));
        httpPost.setEntity(body);
        final var response = httpClient.execute(httpPost);
        final var entity = response.getEntity();
        if (null != entity) {
            System.out.println(EntityUtils.toString(entity, "UTF-8"));
        }
    }

    /*
     * Helper method to get existing rules
     * */
    List<String> getRules(String bearerToken) throws URISyntaxException, IOException, JSONException, ParseException {
        List<String> rules = new ArrayList<>();
        final var httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec("STANDARD").build())
                .build();

        final var uriBuilder = new URIBuilder(configData.getTwitterV2RulesBaseUrl());

        final var httpGet = new HttpGet(uriBuilder.build());
        httpGet.setHeader("Authorization", String.format("Bearer %s", bearerToken));
        httpGet.setHeader("content-type", "application/json");
        final var response = httpClient.execute(httpGet);
        final var entity = response.getEntity();
        if (null != entity) {
            final var json = new JSONObject(EntityUtils.toString(entity, "UTF-8"));
            if (json.length() > 1 && json.has("data")) {
                final var array = (JSONArray) json.get("data");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject jsonObject = (JSONObject) array.get(i);
                    rules.add(jsonObject.getString("id"));
                }
            }
        }
        return rules;
    }

    /*
     * Helper method to delete rules
     * */
    void deleteRules(final String bearerToken, final List<String> existingRules) throws URISyntaxException, IOException, ParseException {
        final var httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec("STANDARD").build())
                .build();

        final var uriBuilder = new URIBuilder(configData.getTwitterV2RulesBaseUrl());

        final var httpPost = new HttpPost(uriBuilder.build());
        httpPost.setHeader("Authorization", String.format("Bearer %s", bearerToken));
        httpPost.setHeader("content-type", "application/json");
        final var body = new StringEntity(getFormattedString("{ \"delete\": { \"ids\": [%s]}}", existingRules));
        httpPost.setEntity(body);
        final var response = httpClient.execute(httpPost);
        final var entity = response.getEntity();
        if (null != entity) {
            System.out.println(EntityUtils.toString(entity, "UTF-8"));
        }
    }

    String getFormattedString(final String string, final List<String> ids) {
        final var sb = new StringBuilder();
        if (ids.size() == 1) {
            return String.format(string, "\"" + ids.get(0) + "\"");
        } else {
            for (String id : ids) {
                sb.append("\"" + id + "\"" + ",");
            }
            final var result = sb.toString();
            return String.format(string, result.substring(0, result.length() - 1));
        }
    }

    String getFormattedString(final String string, final Map<String, String> rules) {
        final var sb = new StringBuilder();
        if (rules.size() == 1) {
            final var key = rules.keySet().iterator().next();
            return String.format(string, "{\"value\": \"" + key + "\", \"tag\": \"" + rules.get(key) + "\"}");
        } else {
            for (Map.Entry<String, String> entry : rules.entrySet()) {
                final var value = entry.getKey();
                final var tag = entry.getValue();
                sb.append("{\"value\": \"" + value + "\", \"tag\": \"" + tag + "\"}" + ",");
            }
            final var result = sb.toString();
            return String.format(string, result.substring(0, result.length() - 1));
        }
    }

    private String getFormattedTweet(final String data) throws JSONException {
        final var jsonData = (JSONObject)new JSONObject(data).get("data");
        final var params = new String[]{
                ZonedDateTime.parse(jsonData.get("created_At").toString()).withZoneSameInstant(ZoneId.of("UTC"))
                        .format(DateTimeFormatter.ofPattern(TWITTER_STATUS_DATE_FORMAT, Locale.ENGLISH)),
                jsonData.get("id").toString(),
                jsonData.get("text").toString().replaceAll("\"", "\\\\\""),
                jsonData.get("author_id").toString()
        };
        return formattedTweetAsJsonWithParams(params);
    }

    private String formattedTweetAsJsonWithParams(final String[] params) {
        var tweet = TWEET_AS_RAW_JSON;
        for(int i = 0; i < params.length; i++) {
            tweet = tweet.replace("{" + i + "}", params[i]);
        }
        return tweet;
    }
}
