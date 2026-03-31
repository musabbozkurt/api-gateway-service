package com.mb.notificationservice.service;

import java.util.Map;

public interface ThymeleafTemplateService {

    String processTemplate(String templateContent, Map<String, Object> variables);
}
