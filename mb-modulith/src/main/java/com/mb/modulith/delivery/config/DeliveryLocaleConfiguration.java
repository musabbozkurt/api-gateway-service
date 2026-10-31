package com.mb.modulith.delivery.config;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Locale;
import java.util.Objects;

@Configuration
public class DeliveryLocaleConfiguration {

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver() {
            @Override
            public @NonNull Locale resolveLocale(HttpServletRequest request) {
                String language = request.getHeader("Accept-Language");
                if (StringUtils.isNotBlank(language)) {
                    return Locale.forLanguageTag(language);
                }
                Locale defaultLocale = getDefaultLocale();
                return Objects.nonNull(defaultLocale) ? defaultLocale : Locale.US;
            }
        };
        localeResolver.setDefaultLocale(Locale.US);
        return localeResolver;
    }
}
