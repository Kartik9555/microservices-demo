package com.microservices.demo.elastic.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.microservices.demo.config.ElasticConfigData;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Objects;

@Configuration
@RequiredArgsConstructor
public class ElasticsearchConfig {

    private final ElasticConfigData elasticConfigData;

//    @Bean
//    public RestHighLevelClient elasticsearchClient() {
//        final var uri = UriComponentsBuilder.fromUriString(elasticConfigData.getConnectionUrl()).build();
//        return new RestHighLevelClient(
//                RestClient.builder(new HttpHost(
//                        Objects.requireNonNull(uri.getHost()),
//                        uri.getPort(),
//                        uri.getScheme()
//                )).setRequestConfigCallback(config ->
//                        config.setConnectTimeout(elasticConfigData.getConnectTimeoutMs())
//                                .setSocketTimeout(elasticConfigData.getSocketTimeoutMs())
//                )
//        );
//    }

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        final var uri = UriComponentsBuilder.fromUriString(elasticConfigData.getConnectionUrl()).build();
        final var restclient = RestClient.builder(new HttpHost(
                Objects.requireNonNull(uri.getHost()),
                uri.getPort(),
                uri.getScheme()
        )).setRequestConfigCallback(config ->
                config.setConnectTimeout(elasticConfigData.getConnectTimeoutMs())
                        .setSocketTimeout(elasticConfigData.getSocketTimeoutMs())
        ).build();
        final var restClientTransport =  new RestClientTransport(restclient, new JacksonJsonpMapper());
        return new ElasticsearchClient(restClientTransport);
    }
}
