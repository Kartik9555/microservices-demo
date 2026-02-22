package com.microservices.demo.elastic.query.web.client.config;

import com.microservices.demo.config.ElasticQueryWebClientConfigData;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import org.apache.hc.core5.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
@LoadBalancerClient(name = "elastic-query-service", configuration = ElasticQueryServiceInstanceListSupplierConfig.class)
public class WebClientConfig {

    @Value("${security.default-client-registration-id}")
    private String defaultClientRegistrationId;
    private final ElasticQueryWebClientConfigData elasticQueryWebClientConfigData;

    @LoadBalanced
    @Bean("webClientBuilder")
    public WebClient.Builder webClientBuilder(ClientRegistrationRepository clientRegistrationRepository, OAuth2AuthorizedClientRepository oAuth2AuthorizedClientRepository) {
        final var filterFunction = new ServletOAuth2AuthorizedClientExchangeFilterFunction(clientRegistrationRepository, oAuth2AuthorizedClientRepository);
        filterFunction.setDefaultOAuth2AuthorizedClient(true);
        filterFunction.setDefaultClientRegistrationId(defaultClientRegistrationId);

        return WebClient.builder()
                .baseUrl(elasticQueryWebClientConfigData.getWebClient().getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, elasticQueryWebClientConfigData.getWebClient().getContentType())
                .defaultHeader(HttpHeaders.ACCEPT, elasticQueryWebClientConfigData.getWebClient().getAcceptType())
                .clientConnector(new ReactorClientHttpConnector(getHttpClient()))
                .apply(filterFunction.oauth2Configuration())
                .codecs(clientCodecConfigurer -> clientCodecConfigurer.defaultCodecs().maxInMemorySize(elasticQueryWebClientConfigData.getWebClient().getMaxInMemorySize()));
    }

    private HttpClient getHttpClient() {
        return HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, elasticQueryWebClientConfigData.getWebClient().getConnectionTimeoutMs())
                .doOnConnected(connection -> {
                    connection.addHandlerLast(new ReadTimeoutHandler(elasticQueryWebClientConfigData.getWebClient().getReadTimeoutMs(), TimeUnit.MILLISECONDS));
                    connection.addHandlerLast(new WriteTimeoutHandler(elasticQueryWebClientConfigData.getWebClient().getWriteTimeoutMs(), TimeUnit.MILLISECONDS));
                });
    }
}
