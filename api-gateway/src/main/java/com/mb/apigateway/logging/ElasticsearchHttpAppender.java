package com.mb.apigateway.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.AppenderBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple local/testing appender: sends logs directly to the Elasticsearch index endpoint.
 */
@Slf4j
public class ElasticsearchHttpAppender extends AppenderBase<ILoggingEvent> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    // Example: http://localhost:9200/logstash-api-gateway-service
    @Setter
    private String indexUrl;

    @Override
    protected void append(ILoggingEvent event) {
        try {
            if (StringUtils.isBlank(indexUrl)) {
                log.info("Elasticsearch index URL is not configured. Skipping log sending.");
                return;
            }
            Map<String, Object> payload = new LinkedHashMap<>();
            String ts = Instant.ofEpochMilli(event.getTimeStamp()).toString();
            payload.put("@timestamp", ts);
            payload.put("timestamp", ts);
            payload.put("level", event.getLevel().toString());
            payload.put("logger_name", event.getLoggerName());
            payload.put("thread_name", event.getThreadName());
            payload.put("message", event.getFormattedMessage());

            Map<String, String> mdc = event.getMDCPropertyMap();
            if (mdc != null && !mdc.isEmpty()) {
                payload.putAll(mdc);
            }

            IThrowableProxy throwableProxy = event.getThrowableProxy();
            if (throwableProxy != null) {
                payload.put("stack_trace", ThrowableProxyUtil.asString(throwableProxy));
            }

            String body = objectMapper.writeValueAsString(payload);
            String endpoint = normalize(indexUrl) + "/_doc";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() >= 300) {
                addWarn("Elasticsearch indexing failed with status: " + response.statusCode());
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            addError("Interrupted while sending log to Elasticsearch", ex);
        } catch (Exception ex) {
            addError("Failed to send log to Elasticsearch", ex);
        }
    }

    private String normalize(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
