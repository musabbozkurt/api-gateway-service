package com.mb.apigateway.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class SwaggerFilter extends ZuulFilter {

    private static final String SWAGGER_PATH = "/swagger-ui.html";
    private static final String SWAGGER_RESOURCES_PATH = "/webjars/springfox-swagger-ui/";
    private static final String SWAGGER_RESOURCES_UI_PATH = "/swagger-resources";
    private static final String SWAGGER_RESOURCES_UI_PATH_REGEX = ".*".concat(SWAGGER_RESOURCES_UI_PATH).concat("(.*)");
    private static final String SWAGGER_RESOURCES_PATH_REGEX = ".*/webjars/springfox-swagger-ui/(.*)";
    private static final String SWAGGER_RESOURCES_DOCUMENTATION_PATH = "/v2/api-docs";
    private static final String REQUEST_URI_KEY = "requestURI";

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return FilterConstants.PRE_DECORATION_FILTER_ORDER + 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        String previousUri = request.getRequestURI();

        if (previousUri.endsWith(SWAGGER_PATH)) {
            ctx.set(REQUEST_URI_KEY, SWAGGER_PATH);
        } else if (previousUri.contains(SWAGGER_RESOURCES_PATH)) {
            ctx.set(REQUEST_URI_KEY, previousUri.replaceAll(SWAGGER_RESOURCES_PATH_REGEX, SWAGGER_RESOURCES_PATH.concat("$1")));
        } else if (previousUri.contains(SWAGGER_RESOURCES_UI_PATH)) {
            ctx.set(REQUEST_URI_KEY, previousUri.replaceAll(SWAGGER_RESOURCES_UI_PATH_REGEX, SWAGGER_RESOURCES_UI_PATH.concat("$1")));
        } else if (previousUri.contains(SWAGGER_RESOURCES_DOCUMENTATION_PATH)) {
            ctx.set(REQUEST_URI_KEY, SWAGGER_RESOURCES_DOCUMENTATION_PATH);
        }

        return null;
    }
}