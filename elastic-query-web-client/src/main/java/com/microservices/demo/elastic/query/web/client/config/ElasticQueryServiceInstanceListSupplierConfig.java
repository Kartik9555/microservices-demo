package com.microservices.demo.elastic.query.web.client.config;

import com.microservices.demo.config.ElasticQueryWebClientConfigData;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

@Primary
@Configuration
public class ElasticQueryServiceInstanceListSupplierConfig implements ServiceInstanceListSupplier {

    private final ElasticQueryWebClientConfigData.WebClient webClient;

    public ElasticQueryServiceInstanceListSupplierConfig(ElasticQueryWebClientConfigData webClient) {
        this.webClient = webClient.getWebClient()   ;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public String getServiceId() {
        return webClient.getServiceId();
    }

    @Override
    public Flux<List<ServiceInstance>> get() {
        return Flux.just(webClient.getInstances()
                .stream()
                .map(instance -> new DefaultServiceInstance(instance.getId(), getServiceId(), instance.getHost(), instance.getPort(), false))
                .collect(Collectors.toList()));
    }
}
