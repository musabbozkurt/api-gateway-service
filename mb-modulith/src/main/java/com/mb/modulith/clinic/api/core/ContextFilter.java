package com.mb.modulith.clinic.api.core;

import com.mb.modulith.clinic.exception.ErrorResponse;
import com.mb.modulith.shared.exception.BaseException;
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
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Slf4j
@Order(1)
@Component
@RequiredArgsConstructor
public class ContextFilter extends OncePerRequestFilter {

    private static final String USER_ID_HEADER = "userId";
    private static final String DOCTOR_ID_HEADER = "doctorId";
    private static final String PATIENT_ID_HEADER = "patientId";

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest httpServletRequest, @NonNull HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        try {
            Context ctx = ContextHolder.get();
            populateHeaderDetails(ctx, httpServletRequest);
            ContextHolder.set(ctx);
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        } catch (BaseException e) {
            httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
            httpServletResponse.setStatus(e.getErrorCode().getHttpStatus().value());
            httpServletResponse.getWriter().write(objectMapper.writeValueAsString(
                    new ErrorResponse(e.getErrorCode().getCode(), e.getMessage())));
            httpServletResponse.getWriter().flush();
            httpServletResponse.getWriter().close();
        } finally {
            ContextHolder.clear();
            MDC.clear();
        }
    }

    private void populateHeaderDetails(Context context, HttpServletRequest httpServletRequest) {
        String doctorId = getHeader(httpServletRequest, DOCTOR_ID_HEADER);
        context.setDoctorId(StringUtils.isNotBlank(doctorId) ? Integer.parseInt(doctorId) : null);

        String userId = getHeader(httpServletRequest, USER_ID_HEADER);
        context.setDoctorId(StringUtils.isNotBlank(userId) ? Integer.parseInt(userId) : null);

        String patientId = getHeader(httpServletRequest, PATIENT_ID_HEADER);
        context.setDoctorId(StringUtils.isNotBlank(patientId) ? Integer.parseInt(patientId) : null);
    }

    private String getHeader(HttpServletRequest httpServletRequest, String headerName) {
        return httpServletRequest.getHeader(headerName);
    }

    @Override
    public void destroy() {
        log.info("ContextFilter is destroyed.");
    }

}
