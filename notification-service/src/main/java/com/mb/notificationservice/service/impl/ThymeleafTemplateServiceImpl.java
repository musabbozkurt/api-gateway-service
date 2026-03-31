package com.mb.notificationservice.service.impl;

import com.mb.notificationservice.service.ThymeleafTemplateService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.util.Map;
import java.util.Objects;

@Service
public class ThymeleafTemplateServiceImpl implements ThymeleafTemplateService {

    private final TemplateEngine stringTemplateEngine;

    public ThymeleafTemplateServiceImpl() {
        StringTemplateResolver stringTemplateResolver = new StringTemplateResolver();
        stringTemplateResolver.setTemplateMode(TemplateMode.HTML);

        this.stringTemplateEngine = new SpringTemplateEngine();
        this.stringTemplateEngine.setTemplateResolver(stringTemplateResolver);
    }

    @Override
    public String processTemplate(String templateContent, Map<String, Object> variables) {
        if (StringUtils.isBlank(templateContent)) {
            return null;
        }

        Context context = new Context();
        if (Objects.nonNull(variables)) {
            context.setVariables(variables);
        }

        return stringTemplateEngine.process(templateContent, context);
    }
}
