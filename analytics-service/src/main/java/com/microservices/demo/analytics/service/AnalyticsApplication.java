package com.microservices.demo.analytics.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.microservices.demo")
public class AnalyticsApplication {
    static void main() {
        SpringApplication.run(AnalyticsApplication.class);
    }
}
