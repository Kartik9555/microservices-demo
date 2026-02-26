package com.microservices.demo.elastic.query.web.client.config;

import com.microservices.demo.config.ElasticQueryWebClientConfigData;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import static io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.apache.hc.core5.http.HttpHeaders.ACCEPT;
import static org.apache.hc.core5.http.HttpHeaders.CONTENT_TYPE;

@Configuration
@RequiredArgsConstructor
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
                .defaultHeader(CONTENT_TYPE, elasticQueryWebClientConfigData.getWebClient().getContentType())
                .defaultHeader(ACCEPT, elasticQueryWebClientConfigData.getWebClient().getAcceptType())
                .clientConnector(new ReactorClientHttpConnector(getHttpClient()))
                .apply(filterFunction.oauth2Configuration())
                .codecs(clientCodecConfigurer -> clientCodecConfigurer.defaultCodecs().maxInMemorySize(elasticQueryWebClientConfigData.getWebClient().getMaxInMemorySize()));
    }

    private HttpClient getHttpClient() {
        return HttpClient.create()
                .option(CONNECT_TIMEOUT_MILLIS, elasticQueryWebClientConfigData.getWebClient().getConnectionTimeoutMs())
                .doOnConnected(connection -> {
                    connection.addHandlerLast(new ReadTimeoutHandler(elasticQueryWebClientConfigData.getWebClient().getReadTimeoutMs(), MILLISECONDS));
                    connection.addHandlerLast(new WriteTimeoutHandler(elasticQueryWebClientConfigData.getWebClient().getWriteTimeoutMs(), MILLISECONDS));
                });
    }
}
