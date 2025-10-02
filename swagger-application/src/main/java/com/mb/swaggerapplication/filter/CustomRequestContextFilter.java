package com.mb.swaggerapplication.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mb.swaggerapplication.context.ContextHolder;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomRequestContextFilter implements Filter, Ordered {

    private static final String USER_NAME = "username";
    private static final String CLIENT_ID = "client_id";
    private static final String SESSION_ID = "sessionId";

    private final ObjectMapper objectMapper;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException {
        try {
            initializeMDCAndContext(servletRequest);
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (Exception e) {
            servletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
            servletResponse.getWriter().write(objectMapper.writeValueAsString(e.getMessage()));
            servletResponse.getWriter().flush();
            servletResponse.getWriter().close();
        } finally {
            clearMDC();
            ContextHolder.clear();
        }
    }

    @Override
    public void destroy() {
        log.info("CustomOncePerRequestFilter is destroyed.");
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private void initializeMDCAndContext(ServletRequest servletRequest) {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;

        String username = httpRequest.getHeader(USER_NAME);
        String clientId = httpRequest.getHeader(CLIENT_ID);
        String sessionId = httpRequest.getHeader(SESSION_ID);

        ContextHolder.Context.ContextBuilder contextBuilder = ContextHolder.Context.builder();

        if (StringUtils.isNotBlank(username)) {
            MDC.put(USER_NAME, username);
            contextBuilder.username(username);
        }

        if (StringUtils.isNotBlank(clientId)) {
            MDC.put(CLIENT_ID, clientId);
            contextBuilder.clientId(clientId);
        }

        if (StringUtils.isNotBlank(sessionId)) {
            MDC.put(SESSION_ID, sessionId);
            contextBuilder.sessionId(sessionId);
        }

        ContextHolder.setContext(contextBuilder.build());
    }

    private void clearMDC() {
        MDC.remove(USER_NAME);
        MDC.remove(CLIENT_ID);
        MDC.remove(SESSION_ID);
    }
}
