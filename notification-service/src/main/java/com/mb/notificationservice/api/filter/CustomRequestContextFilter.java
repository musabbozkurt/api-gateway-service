package com.mb.notificationservice.api.filter;

import com.mb.notificationservice.api.context.ContextHolder;
import com.mb.notificationservice.util.HeaderUtils;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomRequestContextFilter implements Filter {

    private static final String SESSION_ID = "sessionId";

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        try {
            initializeMDCAndContext(servletRequest);
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            clearMDC();
            ContextHolder.clear();
        }
    }

    private void initializeMDCAndContext(ServletRequest servletRequest) {
        String username = HeaderUtils.getUsername();
        String clientId = HeaderUtils.getClientId();

        if (StringUtils.isNotBlank(username)) {
            MDC.put(HeaderUtils.USER_NAME, username);
        }

        if (StringUtils.isNotBlank(clientId)) {
            MDC.put(HeaderUtils.CLIENT_ID, clientId);
        }

        HttpSession httpSession = ((HttpServletRequest) servletRequest).getSession();
        if (httpSession != null) {
            MDC.put(SESSION_ID, httpSession.getId());
        }

        ContextHolder.setContext(
                ContextHolder.Context.builder()
                        .username(username)
                        .clientId(clientId)
                        .userId(HeaderUtils.getUserId())
                        .build()
        );
    }

    private void clearMDC() {
        MDC.remove(HeaderUtils.USER_NAME);
        MDC.remove(HeaderUtils.CLIENT_ID);
        MDC.remove(SESSION_ID);
    }
}
