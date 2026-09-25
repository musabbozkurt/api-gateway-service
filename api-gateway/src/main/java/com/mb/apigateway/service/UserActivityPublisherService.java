package com.mb.apigateway.service;

import com.mb.apigateway.logging.UserActivityEvent;
import org.springframework.web.server.ServerWebExchange;

public interface UserActivityPublisherService {

    void publish(ServerWebExchange exchange, UserActivityEvent activityEvent);
}
