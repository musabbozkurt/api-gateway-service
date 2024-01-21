package com.mb.openaiservice.config;

import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

@Slf4j
@RequiredArgsConstructor
public class OpenAIErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder errorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {

        Exception exception = errorDecoder.decode(methodKey, response);

        if (HttpStatus.valueOf(response.status()) == HttpStatus.UNAUTHORIZED) {
            log.warn("UNAUTHORIZED OpenAI request.");
            throw new RetryableException(response.status(), exception.getMessage(), response.request().httpMethod(), System.currentTimeMillis(), response.request());
        }

        return exception;
    }
}