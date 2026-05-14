package com.mb.inventorymanagementservice.common.context;

import com.mb.inventorymanagementservice.exception.BaseException;
import com.mb.inventorymanagementservice.exception.InvalidParameterException;
import com.mb.inventorymanagementservice.exception.LocalizedErrorResponse;
import com.mb.inventorymanagementservice.utils.Constants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.MDC;
import org.jspecify.annotations.NonNull;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

@Slf4j
@Order(1)
@Component
@RequiredArgsConstructor
public class ContextFilter extends OncePerRequestFilter {

    public static final String ACCEPT_LANGUAGE_HEADER = "Accept-Language";
    public static final String FORWARDED_FOR_HEADER = "x-forwarded-for";
    public static final String USER_AGENT_HEADER = "user-agent";
    public static final String ZONE_ID_HEADER = "X-ZONE-ID";
    public static final String ADMIN_PERMISSION = "ADMIN";

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            Context context = ContextHolder.get();
            populateHeaderDetails(context, request);
            populateSecurityContext(context);
            ContextHolder.set(context);
            populateTracing();
            filterChain.doFilter(request, response);
        } catch (BaseException e) {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(e.getErrorCode().getHttpStatus().value());
            response.getWriter().write(objectMapper.writeValueAsString(new LocalizedErrorResponse(e.getErrorCode().getCode())));
            response.getWriter().flush();
            response.getWriter().close();
        } finally {
            ContextHolder.clear();
            MDC.clear();
        }
    }

    private void populateHeaderDetails(Context context, HttpServletRequest httpServletRequest) {
        Locale language = getLanguageHeader(httpServletRequest);
        context.setLanguage(language);

        context.setIpAddress(getIpAddress(httpServletRequest));
        context.setUserAgent(getHeader(httpServletRequest, USER_AGENT_HEADER));

        String zoneId = getHeader(httpServletRequest, ZONE_ID_HEADER);
        context.setPreferredZoneOffset(zoneId == null ? ZoneOffset.UTC : TimeZone.getTimeZone(zoneId).toZoneId().getRules().getOffset(Instant.now()));
    }

    private String getHeader(HttpServletRequest httpServletRequest, String headerName) {
        return httpServletRequest.getHeader(headerName);
    }

    private Locale getLanguageHeader(HttpServletRequest request) {
        String value = request.getHeader(ACCEPT_LANGUAGE_HEADER);
        if (StringUtils.isNotBlank(value)) {
            try {
                return Locale.of(value);
            } catch (Exception _) {
                throw new InvalidParameterException();
            }
        }
        return Locale.of("EN");
    }

    private String getIpAddress(HttpServletRequest request) {
        final String xForwardedForHeader = request.getHeader(FORWARDED_FOR_HEADER);
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        }
        final String[] tokenized = xForwardedForHeader.trim().split(",");
        return tokenized.length == 0 ? null : tokenized[0].trim();
    }

    private void populateSecurityContext(Context context) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof UsernamePasswordAuthenticationToken authenticationToken && authenticationToken.getDetails() != null) {
            Object principal = authentication.getPrincipal();
            String username = null;
            if (principal instanceof UserDetails userDetails) {
                username = userDetails.getUsername();
            } else if (principal instanceof String string) {
                username = string;
            }
            if (StringUtils.isNotBlank(username)) {
                context.setAdmin(isAdmin(authentication.getAuthorities()));
                context.setUsername(username);
            }
        }
    }

    private boolean isAdmin(Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream().map(GrantedAuthority::getAuthority).anyMatch(ADMIN_PERMISSION::equals);
    }

    private void populateTracing() {
        if (Objects.nonNull(ContextHolder.get().getUsername())) {
            MDC.put("userName", ContextHolder.get().getUsername());
        }

        if (Objects.nonNull(ContextHolder.get().getIpAddress())) {
            MDC.put("ipAddress", ContextHolder.get().getIpAddress());
        }
    }

    @Override
    public void destroy() {
        log.info("ContextFilter is destroyed.");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return Constants.isUriExcluded(request.getRequestURI());
    }
}
