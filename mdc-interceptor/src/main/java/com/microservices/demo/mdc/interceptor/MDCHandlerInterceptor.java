package com.microservices.demo.mdc.interceptor;

import com.microservices.demo.mdc.config.IdGeneratorConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import static com.microservices.demo.mdc.Constants.CORRELATION_ID_HEADER;
import static com.microservices.demo.mdc.Constants.CORRELATION_ID_KEY;

@Component
@RequiredArgsConstructor
public class MDCHandlerInterceptor implements HandlerInterceptor {

    private final IdGeneratorConfig idGenerator;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        final var correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if(!ObjectUtils.isEmpty(correlationId)){
            MDC.put(CORRELATION_ID_KEY, correlationId);
        } else {
            MDC.put(CORRELATION_ID_KEY, getNewCorrelationId());
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        MDC.remove(CORRELATION_ID_KEY);
    }

    private String getNewCorrelationId() {
        return idGenerator.idGenerator().toString();
    }
}
