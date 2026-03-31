package com.mb.notificationservice.service.impl;

import com.mb.notificationservice.api.request.NotificationRequest;
import com.mb.notificationservice.api.response.NotificationResponse;
import com.mb.notificationservice.data.entity.NotificationTemplate;
import com.mb.notificationservice.enums.NotificationChannel;
import com.mb.notificationservice.service.NotificationStrategy;
import com.mb.notificationservice.service.NotificationTemplateService;
import com.mb.notificationservice.service.ThymeleafTemplateService;
import com.mb.notificationservice.util.EmailUtils;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements NotificationStrategy {

    private final JavaMailSender javaMailSender;
    private final NotificationTemplateService notificationTemplateService;
    private final ThymeleafTemplateService thymeleafTemplateService;

    @Value("${email.from}")
    private String emailFrom;

    @Value("${email.subject.prefix:}")
    private String subjectPrefix;

    @Override
    public NotificationResponse send(NotificationRequest request) {
        String id = UUID.randomUUID().toString();

        try {
            sendEmail(request);

            log.info("Email notification sent successfully. Id: {}, Recipients: {}", id, request.getRecipients());

            return NotificationResponse.builder()
                    .id(id)
                    .channel(NotificationChannel.EMAIL)
                    .success(true)
                    .message("Email sent successfully")
                    .build();
        } catch (Exception e) {
            log.error("Exception occurred while sending email notification. Id: {}, Recipients: {}, Exception: {}", id, request.getRecipients(), ExceptionUtils.getStackTrace(e));

            return NotificationResponse.builder()
                    .id(id)
                    .channel(NotificationChannel.EMAIL)
                    .success(false)
                    .message("Failed to send email: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }

    private void sendEmail(NotificationRequest request) {
        if (!EmailUtils.isValid(request)) {
            log.error("Email is not valid to send: {}", request);
            return;
        }

        try {
            String subject;
            String body;

            if (StringUtils.isNotBlank(request.getTemplateCode())) {
                log.info("Using email template: {}", request.getTemplateCode());
                NotificationTemplate template = notificationTemplateService.findActiveByCode(request.getTemplateCode(), NotificationChannel.EMAIL);
                Map<String, Object> variables = request.getTemplateParameters();

                subject = thymeleafTemplateService.processTemplate(template.getSubject(), variables);
                body = thymeleafTemplateService.processTemplate(template.getBody(), variables);
            } else {
                subject = request.getSubject();
                body = request.getBody();
            }

            boolean isHtml = isHtmlContent(body);

            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

            helper.setFrom(emailFrom);
            helper.setSubject(subjectPrefix + subject);
            helper.setText(body, isHtml);
            helper.setTo(request.getRecipients().toArray(new String[0]));

            Set<String> cc = request.getCc();
            if (!CollectionUtils.isEmpty(cc)) {
                helper.setCc(cc.toArray(new String[0]));
            }

            Set<String> bcc = request.getBcc();
            if (!CollectionUtils.isEmpty(bcc)) {
                helper.setBcc(bcc.toArray(new String[0]));
            }

            javaMailSender.send(mimeMessage);
            log.info("Email sent successfully");
        } catch (Exception e) {
            log.error("Exception occurred while sending email. Exception: {}", ExceptionUtils.getStackTrace(e));
            throw new MailSendException("Failed to send email", e);
        }
    }

    private boolean isHtmlContent(String content) {
        if (StringUtils.isEmpty(content)) {
            return false;
        }

        String trimmed = content.trim().toLowerCase();
        return trimmed.startsWith("<!doctype html") ||
                trimmed.startsWith("<html") ||
                trimmed.contains("<body") ||
                trimmed.contains("<table") ||
                trimmed.contains("<div");
    }
}
