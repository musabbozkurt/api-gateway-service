package com.mb.studentservice.config.hcaptcha;

import com.fasterxml.jackson.annotation.JsonProperty;
import feign.RequestInterceptor;
import feign.codec.EncodeException;
import feign.codec.Encoder;
import org.springframework.context.annotation.Bean;
import org.springframework.util.MultiValueMap;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

/**
 * Feign configuration specific to HCaptchaClient.
 * NOT annotated with @Configuration to avoid being component-scanned as a global Feign config.
 * This ensures the OAuth2 RequestInterceptor from OAuthFeignConfig is not applied to hCaptcha requests.
 */
public class HCaptchaCoreFeignConfiguration {

    private static String encodeMultiValueMap(MultiValueMap<?, ?> map) {
        StringJoiner joiner = new StringJoiner("&");
        map.forEach((key, values) -> values.forEach(value ->
                joiner.add(encode(key.toString()) + "=" + encode(value != null ? value.toString() : ""))
        ));
        return joiner.toString();
    }

    /**
     * Encodes a POJO to form-url-encoded format using JavaBeans introspection (public getters).
     * Respects @JsonProperty annotations for field name mapping.
     */
    private static String encodePojo(Object object) {
        try {
            StringJoiner joiner = new StringJoiner("&");
            PropertyDescriptor[] descriptors = Introspector.getBeanInfo(object.getClass(), Object.class).getPropertyDescriptors();
            for (PropertyDescriptor descriptor : descriptors) {
                Object value = descriptor.getReadMethod().invoke(object);
                if (value != null) {
                    String fieldName = resolveFieldName(object.getClass(), descriptor);
                    joiner.add(encode(fieldName) + "=" + encode(value.toString()));
                }
            }
            return joiner.toString();
        } catch (IntrospectionException | ReflectiveOperationException e) {
            throw new EncodeException("Failed to encode object: " + object.getClass().getSimpleName(), e);
        }
    }

    /**
     * Resolves the form field name: uses @JsonProperty value if present, otherwise the property name.
     */
    private static String resolveFieldName(Class<?> clazz, PropertyDescriptor descriptor) {
        try {
            var field = clazz.getDeclaredField(descriptor.getName());
            JsonProperty jsonProperty = field.getAnnotation(JsonProperty.class);
            if (jsonProperty != null && !jsonProperty.value().isEmpty()) {
                return jsonProperty.value();
            }
        } catch (NoSuchFieldException _) {
            // Field is not found (e.g., computed property), use property name
        }
        return descriptor.getName();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * Custom form-url-encoded encoder that does NOT use a feign-form (SpringFormEncoder/FormEncoder).
     * Feign-form internally references java.rmi.UnexpectedException, which was removed in Java 25.
     * Handles both MultiValueMap and POJO objects via JavaBeans introspection (public getters).
     */
    @Bean
    Encoder feignFormEncoder() {
        return (object, _, template) -> {
            String formBody = switch (object) {
                case MultiValueMap<?, ?> map -> encodeMultiValueMap(map);
                default -> encodePojo(object);
            };
            template.header("Content-Type", APPLICATION_FORM_URLENCODED_VALUE);
            template.body(formBody);
        };
    }

    /**
     * No-op interceptor to override the global OAuth2 interceptor.
     * HCaptcha uses a secret key in the request body, not Bearer token authentication.
     */
    @Bean
    RequestInterceptor noOpRequestInterceptor() {
        return _ -> { /* no-op: hCaptcha does not require OAuth2 token */ };
    }
}
