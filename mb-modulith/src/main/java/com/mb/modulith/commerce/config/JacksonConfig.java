package com.mb.modulith.commerce.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cloud.openfeign.support.PageJacksonModule;
import org.springframework.cloud.openfeign.support.SortJacksonModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.zalando.jackson.datatype.money.MoneyModule;

@Configuration
@Import(OffsetDateTimeModule.class)
public class JacksonConfig {

    @Bean
    public Module moneyModule() {
        return (new MoneyModule())
                .withAmountFieldName("value")
                .withCurrencyFieldName("currency")
                .withFormattedFieldName("formatted");
    }

    @Bean
    public JavaTimeModule timeModule() {
        return new JavaTimeModule();
    }

    @Bean
    public OffsetDateTimeModule offsetDateTimeModule() {
        return new OffsetDateTimeModule();
    }

    @Bean
    public PageJacksonModule pagedModule() {
        return new PageJacksonModule();
    }

    @Bean
    public SortJacksonModule sortModule() {
        return new SortJacksonModule();
    }

    /*
     * To beautify JSON responses whether you can create the following Bean
     *
     *     @Bean
     *     public Jackson2ObjectMapperBuilder objectMapperBuilder() {
     *         return new Jackson2ObjectMapperBuilder() {
     *             @Override
     *             public void configure(ObjectMapper objectMapper) {
     *                 super.configure(objectMapper);
     *                 objectMapper.enable(SerializationFeature.INDENT_OUTPUT);  // comment this line for disable pretty
     *             }
     *         };
     *     }
     *
     * Or just add the following to the application.yml
     *
     * spring:
     *   jackson:
     *     serialization:
     *       indent-output: true
     *
     * Beautified JSON Example -> http://localhost:8080/actuator
     * Reference -> https://javadeveloperzone.com/spring-boot/spring-jackson-enable-pretty-json/
     *
     */

}
