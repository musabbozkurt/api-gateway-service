package com.mb.studentservice.config.payment;

import com.mb.studentservice.service.TokenStoreService;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PaymentFeignClientRequestInterceptor implements RequestInterceptor {

    private static final String BEARER = "Bearer";
    private static final String AUTHORIZATION = "Authorization";

    private final TokenStoreService tokenStoreService;

    /**
     * Create a template with the header of provided name and extracted extract
     *
     * @see RequestInterceptor#apply(RequestTemplate)
     */
    @Override
    public void apply(RequestTemplate template) {
        template.removeHeader(AUTHORIZATION);
        template.header(AUTHORIZATION, String.format("%s %s", BEARER, getTokenValue()));
    }

    private String getTokenValue() {
        return this.tokenStoreService.getPaymentToken();
    }
}
