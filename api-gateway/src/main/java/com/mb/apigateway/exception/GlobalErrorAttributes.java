package com.mb.apigateway.exception;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Component
public class GlobalErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(request, options);

        Throwable error = getError(request);

        // Determine the HTTP status code
        HttpStatus status;
        if (error instanceof ResponseStatusException responseStatusException) {
            status = HttpStatus.valueOf(responseStatusException.getStatusCode().value());
        } else {
            status = determineHttpStatus(error);
        }

        // Update status code in error attributes
        errorAttributes.put("status", status.value());
        errorAttributes.put("error", status.getReasonPhrase());

        // Add request ID if available
        String requestId = request.exchange().getRequest().getId();
        errorAttributes.put("requestId", requestId);

        return errorAttributes;
    }

    private HttpStatus determineHttpStatus(Throwable error) {
        // Check for @ResponseStatus annotation
        ResponseStatus responseStatus = MergedAnnotations
                .from(error.getClass(), MergedAnnotations.SearchStrategy.TYPE_HIERARCHY)
                .get(ResponseStatus.class)
                .synthesize();

        if (HttpStatus.INTERNAL_SERVER_ERROR.equals(responseStatus.code())) {
            return responseStatus.code();
        }

        // Default to internal server error
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
