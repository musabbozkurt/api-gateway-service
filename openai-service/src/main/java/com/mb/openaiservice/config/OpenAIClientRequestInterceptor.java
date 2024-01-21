package com.mb.openaiservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OpenAIClientRequestInterceptor implements RequestInterceptor {

    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";

    private final OpenAIConfigProperties openAIConfigProperties;

    /**
     * Create a template with the header of provided name and extracted extract
     *
     * @see RequestInterceptor#apply(RequestTemplate)
     */
    @Override
    public void apply(RequestTemplate template) {
        template.removeHeader(AUTHORIZATION);
        template.header(AUTHORIZATION, BEARER + openAIConfigProperties.getToken());
    }

}