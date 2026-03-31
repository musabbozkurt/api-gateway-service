package com.mb.notificationservice.config;

import com.mb.notificationservice.client.dummysms.DummySmsClient;
import com.mb.notificationservice.exception.BaseException;
import com.mb.notificationservice.exception.NotificationErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Slf4j
@Configuration
public class DummySmsClientConfig {

    @Bean
    public DummySmsClient dummySmsClient(@Value("${dummy-sms.url}") String url) {
        RestClient restClient = RestClient.builder()
                .baseUrl(url)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .defaultStatusHandler(status -> !status.is2xxSuccessful(), (request, response) -> {
                    log.error("Dummy SMS otp error. HttpStatus: {}", response.getStatusCode());
                    throw new BaseException(NotificationErrorCode.DUMMY_SMS_ERROR);
                })
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();

        return factory.createClient(DummySmsClient.class);
    }
}
