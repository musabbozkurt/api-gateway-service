package com.mb.studentservice.config;

import feign.Util;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

/*
    Feign cares about the dto objects, not the whole Response object.
    This decoder is required to extract dto object from Response object.
    Hence this implementation is required so that we can extract the data object from the response object.

    //java.io.IOException: stream is closed can occur on debug mode
    log.info("Received response {} with status {}", response.toString(), response.status());
 */
@Slf4j
public class ResponseToErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, feign.Response response) {
        String body;
        String responseToString = response.toString();
        // extract body as string from feign response.
        try {
            log.info("Received feign error response. decode - body: {} status: {}.", responseToString, response.status());
            body = Util.toString(response.body().asReader());
            log.warn("Feign Response Error received: decode - body: {}.", body);
            throw new RuntimeException("Feign Response Error received: decode - body:");
        } catch (Exception e) {
            log.error("Exception when deserializing exception. decode - ex: {}", ExceptionUtils.getStackTrace(e));
            throw new RuntimeException("Exception when deserializing exception. decode");
        }
    }
}
