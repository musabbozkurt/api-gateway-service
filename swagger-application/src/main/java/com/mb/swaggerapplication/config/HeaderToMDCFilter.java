package com.mb.swaggerapplication.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class HeaderToMDCFilter implements Filter {

    private static final String USERNAME = "username";
    private static final String CLIENT_ID = "client_id";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String username = httpRequest.getHeader(USERNAME);
        String clientId = httpRequest.getHeader(CLIENT_ID);

        try {
            if (StringUtils.isNotBlank(username)) {
                MDC.put(USERNAME, username);
            }

            if (StringUtils.isNotBlank(clientId)) {
                MDC.put(CLIENT_ID, clientId);
            }

            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
