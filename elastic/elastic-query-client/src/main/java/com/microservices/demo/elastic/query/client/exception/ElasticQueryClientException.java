package com.microservices.demo.elastic.query.client.exception;

public class ElasticQueryClientException extends RuntimeException {
    public ElasticQueryClientException() {
        super();
    }

    public ElasticQueryClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public ElasticQueryClientException(String message) {
        super(message);
    }
}
