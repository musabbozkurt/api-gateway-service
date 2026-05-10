package com.mb.gitlabservice.client.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GitlabClientRequestInterceptor implements RequestInterceptor {

    private static final String PRIVATE_TOKEN = "PRIVATE-TOKEN";

    private final GitlabConfigProperties gitlabConfigProperties;

    /**
     * Create a template with the header of provided name and extracted extract
     *
     * @see RequestInterceptor#apply(RequestTemplate)
     */
    @Override
    public void apply(RequestTemplate template) {
        template.removeHeader(PRIVATE_TOKEN);
        template.header(PRIVATE_TOKEN, gitlabConfigProperties.getPrivateToken());
    }
}
