package com.mb.notificationservice.config;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.mb.notificationservice.queue.dto.NotificationEventDto;
import com.mb.notificationservice.util.ServiceConstants;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        properties = {
                "spring.cloud.config.enabled=false",
                "spring.flyway.enabled=false",
                "spring.sql.init.mode=never",
                "management.tracing.sampling.probability=1.0"
        }
)
@EmbeddedKafka(partitions = 1, topics = {ServiceConstants.NOTIFICATION_TOPIC})
class TracingMdcIntegrationTest {

    @Autowired
    private Tracer tracer;

    @MockitoBean
    private JavaMailSender javaMailSender;

    @MockitoBean
    private KafkaTemplate<String, NotificationEventDto> kafkaTemplate;

    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger(TracingMdcIntegrationTest.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(listAppender);
        listAppender.stop();
    }

    @Test
    void shouldPopulateTraceIdAndSpanIdInMdc_WhenSpanIsActive() {
        Span span = tracer.nextSpan().name("test-trace-propagation").start();
        try (var _ = tracer.withSpan(span)) {
            logger.info("Test log within traced span");

            assertFalse(listAppender.list.isEmpty(), "Should have captured at least one log event");
            ILoggingEvent logEvent = listAppender.list.getLast();

            String traceId = logEvent.getMDCPropertyMap().get("traceId");
            String spanId = logEvent.getMDCPropertyMap().get("spanId");

            assertNotNull(traceId, "traceId should be present in MDC during active span");
            assertNotNull(spanId, "spanId should be present in MDC during active span");
            assertFalse(traceId.isBlank(), "traceId should not be blank");
            assertFalse(spanId.isBlank(), "spanId should not be blank");
        } finally {
            span.end();
        }
    }

    @Test
    void shouldClearTraceIdAndSpanIdFromMdc_WhenSpanScopeIsClosed() {
        Span span = tracer.nextSpan().name("test-mdc-cleanup").start();
        try (var _ = tracer.withSpan(span)) {
            logger.info("Log inside span");
        } finally {
            span.end();
        }

        listAppender.list.clear();
        logger.info("Log after span ended");

        ILoggingEvent afterEvent = listAppender.list.getLast();
        String traceId = afterEvent.getMDCPropertyMap().get("traceId");

        assertTrue(traceId == null || traceId.isBlank(), "traceId should be absent from MDC after span scope is closed");
    }
}
