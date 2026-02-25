package com.microservices.demo.elastic.query.service.business.impl;

import com.microservices.demo.config.ElasticQueryServiceConfigData;
import com.microservices.demo.elastic.model.index.impl.TwitterIndexModel;
import com.microservices.demo.elastic.query.client.service.ElasticQueryClient;
import com.microservices.demo.elastic.query.service.business.ElasticQueryService;
import com.microservices.demo.elastic.query.service.common.exception.ElasticQueryServiceException;
import com.microservices.demo.elastic.query.service.common.model.ElasticQueryServiceResponseModel;
import com.microservices.demo.elastic.query.service.model.ElasticQueryServiceAnalyticsResponseModel;
import com.microservices.demo.elastic.query.service.model.ElasticQueryServiceWordCountResponseModel;
import com.microservices.demo.elastic.query.service.model.assembler.ElasticQueryServiceResponseModelAssembler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.microservices.demo.elastic.query.service.QueryType.ANALYTICS_DATABASE;
import static com.microservices.demo.elastic.query.service.QueryType.KAFKA_STATE_STORE;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Slf4j
@Service
public class TwitterElasticQueryService implements ElasticQueryService {

    private final ElasticQueryServiceResponseModelAssembler elasticQueryServiceResponseModelAssembler;
    private final ElasticQueryClient<TwitterIndexModel> elasticQueryClient;
    private final ElasticQueryServiceConfigData elasticQueryServiceConfigData;
    private final WebClient.Builder webClientBuilder;

    public TwitterElasticQueryService(ElasticQueryServiceResponseModelAssembler elasticQueryServiceResponseModelAssembler, ElasticQueryClient<TwitterIndexModel> elasticQueryClient, ElasticQueryServiceConfigData elasticQueryServiceConfigData, @Qualifier("webClientBuilder") WebClient.Builder webClientBuilder) {
        this.elasticQueryServiceResponseModelAssembler = elasticQueryServiceResponseModelAssembler;
        this.elasticQueryClient = elasticQueryClient;
        this.elasticQueryServiceConfigData = elasticQueryServiceConfigData;
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public ElasticQueryServiceResponseModel getDocumentById(String id) {
        log.info("Querying elasticsearch by id: {}", id);
        return elasticQueryServiceResponseModelAssembler.toModel(elasticQueryClient.getIndexModelById(id));
    }

    @Override
    public ElasticQueryServiceAnalyticsResponseModel getDocumentByText(String text, String accessToken) {
        log.info("Querying elasticsearch by text: {}", text);
        final var responseModels = elasticQueryServiceResponseModelAssembler.toModels(elasticQueryClient.getIndexModelByText(text));
        return ElasticQueryServiceAnalyticsResponseModel.builder()
                .queryResponseModels(responseModels)
                .wordCount(getWordCount(text, accessToken))
                .build();
    }

    @Override
    public List<ElasticQueryServiceResponseModel> getAllDocuments() {
        log.info("Querying all documents in elasticsearch");
        return elasticQueryServiceResponseModelAssembler.toModels(elasticQueryClient.getAllIndexModels());
    }

    private Long getWordCount(String text, String accessToken) {
        if(KAFKA_STATE_STORE.getType().equals(elasticQueryServiceConfigData.getWebClient().getQueryType())) {
            return getWordCoundFromKafkaStateStore(text, accessToken).getWordCount();
        } else if(ANALYTICS_DATABASE.getType().equals(elasticQueryServiceConfigData.getWebClient().getQueryType())) {
            return getWordCountFromAnalyticsDatabase(text, accessToken).getWordCount();
        }
        return 0L;
    }

    private ElasticQueryServiceWordCountResponseModel getWordCountFromAnalyticsDatabase(String text, String accessToken) {
        final var queryFromAnalyticsDatabase = elasticQueryServiceConfigData.getQueryFromAnalyticsDatabase();
        return retrieveResponseModel(text, accessToken, queryFromAnalyticsDatabase);
    }

    private ElasticQueryServiceWordCountResponseModel getWordCoundFromKafkaStateStore(String text, String accessToken) {
        final var queryFromKafkaStore = elasticQueryServiceConfigData.getQueryFromKafkaStateStore();
        return retrieveResponseModel(text, accessToken, queryFromKafkaStore);
    }

    private ElasticQueryServiceWordCountResponseModel retrieveResponseModel(String text, String accessToken, ElasticQueryServiceConfigData.Query queryFromKafkaStore) {
        log.info("Retrieving word count {} from kafka store: {}", text, accessToken);
        return webClientBuilder.build()
                .method(HttpMethod.valueOf(queryFromKafkaStore.getMethod()))
                .uri(queryFromKafkaStore.getUri(), uriBuilder -> uriBuilder.build(text))
                .headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
                .accept(MediaType.valueOf(queryFromKafkaStore.getAccept()))
                .retrieve()
                .onStatus(
                        s -> s.equals(UNAUTHORIZED),
                        clientResponse -> Mono.just(new BadCredentialsException("Not authenticated"))
                )
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        clientResponse -> Mono.just(new ElasticQueryServiceException(clientResponse.statusCode().toString()))
                )
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        clientResponse -> Mono.just(new ElasticQueryServiceException(clientResponse.statusCode().toString()))
                )
                .bodyToMono(ElasticQueryServiceWordCountResponseModel.class)
                .log()
                .block();
    }
}
