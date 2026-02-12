package com.microservices.demo.elastic.query.web.client.config;

import com.microservices.demo.config.ElasticQueryWebClientConfigData;
import com.microservices.demo.config.UserConfigData;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import org.apache.hc.core5.http.HttpHeaders;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
@LoadBalancerClient(name = "elastic-query-service", configuration = ElasticQueryServiceInstanceListSupplierConfig.class)
public class WebClientConfig {

    private final ElasticQueryWebClientConfigData elasticQueryWebClientConfigData;
    private final UserConfigData userConfigData;

    @LoadBalanced
    @Bean("webClientBuilder")
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .filter(ExchangeFilterFunctions.basicAuthentication(userConfigData.getUsername(), userConfigData.getPassword()))
                .baseUrl(elasticQueryWebClientConfigData.getWebClient().getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, elasticQueryWebClientConfigData.getWebClient().getContentType())
                .defaultHeader(HttpHeaders.ACCEPT, elasticQueryWebClientConfigData.getWebClient().getAcceptType())
                .clientConnector(new ReactorClientHttpConnector(HttpClient.from(getTcpClient())))
                .codecs(clientCodecConfigurer -> clientCodecConfigurer.defaultCodecs().maxInMemorySize(elasticQueryWebClientConfigData.getWebClient().getMaxInMemorySize()));
    }

    private TcpClient getTcpClient() {
        return TcpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, elasticQueryWebClientConfigData.getWebClient().getConnectionTimeoutMs())
                .doOnConnected(connection -> {
                    connection.addHandlerLast(new ReadTimeoutHandler(elasticQueryWebClientConfigData.getWebClient().getReadTimeoutMs(), TimeUnit.MILLISECONDS));
                    connection.addHandlerLast(new WriteTimeoutHandler(elasticQueryWebClientConfigData.getWebClient().getWriteTimeoutMs(), TimeUnit.MILLISECONDS));
                });
    }
}
