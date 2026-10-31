package com.mb.modulith.localstack.client.third_party_api;

import feign.DefaultRetryer;
import feign.Logger.Level;
import feign.micrometer.MicrometerObservationCapability;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.FeignClientBuilder;
import org.springframework.cloud.openfeign.security.OAuth2AccessTokenInterceptor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.util.Optional.ofNullable;

@Configuration
public class ThirdPartyApiClientConfiguration {

    @Bean
    public ThirdPartyApiClient thirdPartyApiClient(final ApplicationContext applicationContext,
                                                   @Autowired(required = false) final MicrometerObservationCapability micrometerObservationCapability,
                                                   @Qualifier("m2mInternalApiRequestInterceptor") final OAuth2AccessTokenInterceptor oAuth2AccessTokenInterceptor,
                                                   @Value("${third-party-client-api.url}") final String url) {
        final FeignClientBuilder feignClientBuilder = new FeignClientBuilder(applicationContext);
        return feignClientBuilder
                .forType(ThirdPartyApiClient.class, "thirdPartyApiClient")
                .url(url)
                .customize(builder -> {
                    builder
                            .dismiss404()
                            .logLevel(Level.HEADERS)
                            .retryer(new DefaultRetryer(100, 250, 2))
                            .requestInterceptor(oAuth2AccessTokenInterceptor);
                    ofNullable(micrometerObservationCapability).ifPresent(builder::addCapability);
                })
                .build();
    }
}
