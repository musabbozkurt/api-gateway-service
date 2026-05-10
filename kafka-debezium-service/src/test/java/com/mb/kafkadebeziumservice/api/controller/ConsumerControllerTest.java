package com.mb.kafkadebeziumservice.api.controller;

import com.mb.kafkadebeziumservice.queue.consumer.ConsumerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(ConsumerController.class)
class ConsumerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConsumerService mockConsumerService;

    @Test
    void testHealth() throws Exception {
        // Setup
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(get("/consumer")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo("Consumer service is up.");
    }

    @Test
    void testConsumeOrders() throws Exception {
        // Setup
        List<String> value = List.of("value");
        when(mockConsumerService.consumeOrders()).thenReturn(value);

        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(get("/consumer/orders")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).contains(value);
    }

    @Test
    void testConsumeOrders_ConsumerServiceReturnsNoItems() throws Exception {
        // Setup
        when(mockConsumerService.consumeOrders()).thenReturn(Collections.emptyList());

        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(get("/consumer/orders")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentLength()).isZero();
    }
}
