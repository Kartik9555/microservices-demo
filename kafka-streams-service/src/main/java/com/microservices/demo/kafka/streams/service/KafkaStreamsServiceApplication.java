package com.microservices.demo.kafka.streams.service;

import com.microservices.demo.kafka.streams.service.init.StreamsInitializer;
import com.microservices.demo.kafka.streams.service.runner.StreamsRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@Slf4j
@EnableDiscoveryClient
@SpringBootApplication
@ComponentScan(basePackages = "com.microservices.demo")
@RequiredArgsConstructor
public class KafkaStreamsServiceApplication implements CommandLineRunner {

    private final StreamsRunner<String, Long> streamsRunner;
    private final StreamsInitializer streamsInitializer;

    static void main() {
        SpringApplication.run(KafkaStreamsServiceApplication.class);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting Kafka Streams Service Application");
        var initResult = streamsInitializer.init();
        if(initResult) {
            log.info("Starting Kafka Stream Runner");
            streamsRunner.start();
        } else {
            log.error("Stream initializer failed to initialize the streams! Not starting the Kafka Streams Runner!");
        }
    }
}
