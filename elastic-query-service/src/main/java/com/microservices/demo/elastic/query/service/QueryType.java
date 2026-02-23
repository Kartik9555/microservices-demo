package com.microservices.demo.elastic.query.service;

import lombok.Getter;

@Getter
public enum QueryType {
    KAFKA_STATE_STORE("KAFKA_STATE_STORE"),
    ANALYTICS_DATABASE("ANALYTICS_DATABASE");

    private String type;

    QueryType(String type) {
        this.type = type;
    }
}
