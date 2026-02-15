package com.microservices.demo.elastic.query.web.client.common.exception;

public class ElasticQueryWebClientException extends RuntimeException {
    public ElasticQueryWebClientException(String message) {
        super(message);
    }

    public ElasticQueryWebClientException() {
    }

    public ElasticQueryWebClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
