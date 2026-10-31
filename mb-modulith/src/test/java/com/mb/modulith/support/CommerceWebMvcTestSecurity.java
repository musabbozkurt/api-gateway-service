package com.mb.modulith.support;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Commerce {@code @WebMvcTest} slices: security filters are disabled so controller tests
 * do not require JWT; production CORS is configured in {@code WebSecurityConfig}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@AutoConfigureMockMvc(addFilters = false)
public @interface CommerceWebMvcTestSecurity {
}
