package com.mb.notificationservice.service.impl;

import com.mb.notificationservice.api.request.NotificationRequest;
import com.mb.notificationservice.data.entity.NotificationTemplate;
import com.mb.notificationservice.service.NotificationTemplateResolver;
import com.mb.notificationservice.service.NotificationTemplateService;
import com.mb.notificationservice.service.ThymeleafTemplateService;
import com.mb.notificationservice.util.ContentUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationTemplateResolverImpl implements NotificationTemplateResolver {

    private final NotificationTemplateService notificationTemplateService;
    private final ThymeleafTemplateService thymeleafTemplateService;

    @Override
    public void resolve(NotificationRequest request) {
        if (StringUtils.isBlank(request.getTemplateCode())) {
            return;
        }

        log.info("Resolving template: {} for channel: {}", request.getTemplateCode(), request.getChannel());

        NotificationTemplate template = notificationTemplateService.findActiveByCode(request.getTemplateCode(), request.getChannel());

        Map<String, Object> variables = request.getTemplateParameters();

        String subject = resolveContent(template.getSubject(), variables);
        String body = resolveContent(template.getBody(), variables);

        request.setSubject(subject);
        request.setBody(body);
        request.setTemplateCode(null);
    }

    private String resolveContent(String content, Map<String, Object> variables) {
        if (StringUtils.isBlank(content)) {
            return content;
        }

        if (ContentUtils.isHtml(content)) {
            return thymeleafTemplateService.processTemplate(content, variables);
        }

        return replacePlaceholders(content, variables);
    }

    private String replacePlaceholders(String content, Map<String, Object> variables) {
        if (Objects.isNull(variables) || variables.isEmpty()) {
            return content;
        }

        String result = content;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = Objects.nonNull(entry.getValue()) ? entry.getValue().toString() : "";
            result = result.replace(placeholder, value);
        }
        return result;
    }
}
