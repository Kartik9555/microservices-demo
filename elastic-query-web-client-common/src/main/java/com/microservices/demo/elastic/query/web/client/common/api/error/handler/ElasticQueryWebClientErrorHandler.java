package com.microservices.demo.elastic.query.web.client.common.api.error.handler;

import com.microservices.demo.elastic.query.web.client.common.model.ElasticQueryWebClientRequestModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;

@Slf4j
@ControllerAdvice
public class ElasticQueryWebClientErrorHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public String handle(AccessDeniedException exception, Model model) {
        log.error("Access Denied Exception", exception);
        model.addAttribute("error", HttpStatus.UNAUTHORIZED.getReasonPhrase());
        model.addAttribute("error_description", "You are not authorized to access this resource!");
        return "error";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handle(IllegalArgumentException exception, Model model) {
        log.error("Illegal Argument Exception", exception);
        model.addAttribute("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        model.addAttribute("error_description", "Illegal Argument Exception! " + exception.getMessage());
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handle(Exception exception, Model model) {
        log.error("Internal Server Error", exception);
        model.addAttribute("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        model.addAttribute("error_description", "A server error occurred!");
        return "error";
    }

    @ExceptionHandler(RuntimeException.class)
    public String handle(RuntimeException exception, Model model) {
        log.error("Service runtime exception!", exception);
        model.addAttribute("elasticQueryWebClientRequestModel", ElasticQueryWebClientRequestModel.builder().build());
        model.addAttribute("error", "Could not get response!" + exception.getMessage());
        model.addAttribute("error_description", "Service runtime exception! " + exception.getMessage());
        return "home";
    }

    @ExceptionHandler(BindException.class)
    public String handle(BindException exception, Model model) {
        log.error("Method argument validation exception!", exception);
        final var errors = new HashMap<String, String>();
        exception.getBindingResult().getAllErrors().forEach((error) -> errors.put(((FieldError)error).getField(), error.getDefaultMessage()));
        model.addAttribute("elasticQueryWebClientRequestModel", ElasticQueryWebClientRequestModel.builder().build());
        model.addAttribute("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        model.addAttribute("error_description", errors);
        return "home";
    }
}