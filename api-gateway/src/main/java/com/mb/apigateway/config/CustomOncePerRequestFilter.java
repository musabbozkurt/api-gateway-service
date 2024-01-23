package com.mb.apigateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Order(1)
@Component
@RequiredArgsConstructor
public class CustomOncePerRequestFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest,
                                    HttpServletResponse httpServletResponse,
                                    FilterChain filterChain) throws IOException {
        try {
            // Adding key into every log lines per request so logs can be tracked easily between microservices.

            MDC.put("adding-key-to-track-logs", "mbmb");
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        } catch (Exception e) {
            httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
            httpServletResponse.getWriter().write(objectMapper.writeValueAsString(e.getMessage()));
            httpServletResponse.getWriter().flush();
            httpServletResponse.getWriter().close();
        } finally {
            MDC.clear();
        }
    }

    @Override
    public void destroy() {
        log.info("CustomOncePerRequestFilter is destroyed.");
    }

}
