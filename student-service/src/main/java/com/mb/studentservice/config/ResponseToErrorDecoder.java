package com.mb.studentservice.config;

import feign.FeignException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * Maps non-success Feign responses to {@link FeignException} so callers receive status and body from the upstream service.
 */
@Slf4j
public class ResponseToErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        log.info("Received feign error response. methodKey={} status={}.", methodKey, response.status());
        FeignException feignException = FeignException.errorStatus(methodKey, response);
        log.warn("Feign response error. methodKey={} body={}.", methodKey, feignException.contentUTF8());
        return feignException;
    }
}
