package com.microservices.demo.elastic.query.service.common.api.error.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ElasticQueryServiceErrorHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handle(AccessDeniedException exception) {
        log.error("Access denied exception!", exception);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to access this resource.");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handle(IllegalArgumentException exception) {
        log.error("Illegal argument exception!", exception);
        return ResponseEntity.badRequest().body("Illegal argument exception! " + exception.getMessage());

    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handle(RuntimeException exception) {
        log.error("Service runtime exception!", exception);
        return ResponseEntity.badRequest().body("Service runtime exception! " + exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handle(Exception exception) {
        log.error("Internal server error!", exception);
        return ResponseEntity.internalServerError().body("Internal server error! " + exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handle(MethodArgumentNotValidException exception) {
        log.error("Method argument validation exception!", exception);
        final var errors = new HashMap<String, String> ();
        exception.getBindingResult().getFieldErrors().forEach((fieldError) -> {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(errors);
    }
}
