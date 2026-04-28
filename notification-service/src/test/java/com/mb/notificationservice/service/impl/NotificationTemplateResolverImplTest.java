package com.mb.notificationservice.service.impl;

import com.mb.notificationservice.api.request.NotificationRequest;
import com.mb.notificationservice.data.entity.NotificationTemplate;
import com.mb.notificationservice.enums.NotificationChannel;
import com.mb.notificationservice.service.NotificationTemplateService;
import com.mb.notificationservice.service.ThymeleafTemplateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationTemplateResolverImplTest {

    @InjectMocks
    private NotificationTemplateResolverImpl notificationTemplateResolver;

    @Mock
    private NotificationTemplateService notificationTemplateService;

    @Mock
    private ThymeleafTemplateService thymeleafTemplateService;

    @Test
    void resolve_ShouldDoNothing_WhenTemplateCodeIsBlank() {
        NotificationRequest request = new NotificationRequest();
        request.setChannel(NotificationChannel.EMAIL);
        request.setSubject("Original Subject");
        request.setBody("Original Body");

        notificationTemplateResolver.resolve(request);

        assertEquals("Original Subject", request.getSubject());
        assertEquals("Original Body", request.getBody());
        verifyNoInteractions(notificationTemplateService);
        verifyNoInteractions(thymeleafTemplateService);
    }

    @Test
    void resolve_ShouldUseThymeleaf_WhenTemplateBodyIsHtml() {
        NotificationTemplate template = new NotificationTemplate();
        template.setSubject("[[${name}]] - Order Confirmed");
        template.setBody("<div>Hello [[${name}]], your order is confirmed.</div>");

        NotificationRequest request = new NotificationRequest();
        request.setChannel(NotificationChannel.EMAIL);
        request.setTemplateCode("ORDER_CONFIRM");
        request.setTemplateParameters(Map.of("name", "John"));

        when(notificationTemplateService.findActiveByCode("ORDER_CONFIRM", NotificationChannel.EMAIL)).thenReturn(template);
        when(thymeleafTemplateService.processTemplate(template.getSubject(), Map.of("name", "John"))).thenReturn("John - Order Confirmed");
        when(thymeleafTemplateService.processTemplate(template.getBody(), Map.of("name", "John"))).thenReturn("<div>Hello John, your order is confirmed.</div>");

        notificationTemplateResolver.resolve(request);

        assertEquals("John - Order Confirmed", request.getSubject());
        assertEquals("<div>Hello John, your order is confirmed.</div>", request.getBody());
        assertNull(request.getTemplateCode());
        verify(thymeleafTemplateService).processTemplate(template.getSubject(), Map.of("name", "John"));
        verify(thymeleafTemplateService).processTemplate(template.getBody(), Map.of("name", "John"));
    }

    @Test
    void resolve_ShouldReplacePlaceholders_WhenTemplateIsNotHtml() {
        NotificationTemplate template = new NotificationTemplate();
        template.setSubject("Hello {{name}}");
        template.setBody("Your order {{orderId}} has been shipped to {{address}}.");

        NotificationRequest request = new NotificationRequest();
        request.setChannel(NotificationChannel.SMS);
        request.setTemplateCode("ORDER_SHIPPED");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "John");
        params.put("orderId", "12345");
        params.put("address", "Istanbul");
        request.setTemplateParameters(params);

        when(notificationTemplateService.findActiveByCode("ORDER_SHIPPED", NotificationChannel.SMS)).thenReturn(template);

        notificationTemplateResolver.resolve(request);

        assertEquals("Hello John", request.getSubject());
        assertEquals("Your order 12345 has been shipped to Istanbul.", request.getBody());
        assertNull(request.getTemplateCode());
        verify(thymeleafTemplateService, never()).processTemplate(anyString(), anyMap());
    }

    @Test
    void resolve_ShouldHandleNullVariables_WhenTemplateIsNotHtml() {
        NotificationTemplate template = new NotificationTemplate();
        template.setSubject("Welcome");
        template.setBody("Hello {{name}}, welcome!");

        NotificationRequest request = new NotificationRequest();
        request.setChannel(NotificationChannel.PUSH);
        request.setTemplateCode("WELCOME");
        request.setTemplateParameters(null);

        when(notificationTemplateService.findActiveByCode("WELCOME", NotificationChannel.PUSH)).thenReturn(template);

        notificationTemplateResolver.resolve(request);

        assertEquals("Welcome", request.getSubject());
        assertEquals("Hello {{name}}, welcome!", request.getBody());
        assertNull(request.getTemplateCode());
    }

    @Test
    void resolve_ShouldHandleEmptyVariables_WhenTemplateIsNotHtml() {
        NotificationTemplate template = new NotificationTemplate();
        template.setSubject("Notification");
        template.setBody("You have a new notification.");

        NotificationRequest request = new NotificationRequest();
        request.setChannel(NotificationChannel.EMAIL);
        request.setTemplateCode("SIMPLE");
        request.setTemplateParameters(Map.of());

        when(notificationTemplateService.findActiveByCode("SIMPLE", NotificationChannel.EMAIL)).thenReturn(template);

        notificationTemplateResolver.resolve(request);

        assertEquals("Notification", request.getSubject());
        assertEquals("You have a new notification.", request.getBody());
        assertNull(request.getTemplateCode());
    }

    @Test
    void resolve_ShouldHandleNullValueInVariables_WhenTemplateIsNotHtml() {
        NotificationTemplate template = new NotificationTemplate();
        template.setSubject("Hello {{name}}");
        template.setBody("Status: {{status}}");

        NotificationRequest request = new NotificationRequest();
        request.setChannel(NotificationChannel.SMS);
        request.setTemplateCode("STATUS");

        Map<String, Object> params = new HashMap<>();
        params.put("name", "John");
        params.put("status", null);
        request.setTemplateParameters(params);

        when(notificationTemplateService.findActiveByCode("STATUS", NotificationChannel.SMS)).thenReturn(template);

        notificationTemplateResolver.resolve(request);

        assertEquals("Hello John", request.getSubject());
        assertEquals("Status: ", request.getBody());
        assertNull(request.getTemplateCode());
    }

    @Test
    void resolve_ShouldUseThymeleaf_WhenSubjectContainsThymeleafExpression() {
        NotificationTemplate template = new NotificationTemplate();
        template.setSubject("[[${count}]] new notifications");
        template.setBody("You have {{count}} pending items.");

        NotificationRequest request = new NotificationRequest();
        request.setChannel(NotificationChannel.EMAIL);
        request.setTemplateCode("NOTIFY");
        request.setTemplateParameters(Map.of("count", "5"));

        when(notificationTemplateService.findActiveByCode("NOTIFY", NotificationChannel.EMAIL)).thenReturn(template);
        when(thymeleafTemplateService.processTemplate(template.getSubject(), Map.of("count", "5"))).thenReturn("5 new notifications");

        notificationTemplateResolver.resolve(request);

        assertEquals("5 new notifications", request.getSubject());
        assertEquals("You have 5 pending items.", request.getBody());
        assertNull(request.getTemplateCode());
    }

    @Test
    void resolve_ShouldHandleBlankSubject_WhenTemplateSubjectIsNull() {
        NotificationTemplate template = new NotificationTemplate();
        template.setSubject(null);
        template.setBody("Message body {{var}}");

        NotificationRequest request = new NotificationRequest();
        request.setChannel(NotificationChannel.PUSH);
        request.setTemplateCode("PUSH_MSG");
        request.setTemplateParameters(Map.of("var", "value"));

        when(notificationTemplateService.findActiveByCode("PUSH_MSG", NotificationChannel.PUSH)).thenReturn(template);

        notificationTemplateResolver.resolve(request);

        assertNull(request.getSubject());
        assertEquals("Message body value", request.getBody());
        assertNull(request.getTemplateCode());
    }

    @Test
    void resolve_ShouldUseThymeleafWithNullParameters_WhenTemplateIsHtml() {
        NotificationTemplate template = new NotificationTemplate();
        template.setSubject("<div>Welcome</div>");
        template.setBody("<div>Simple body without placeholders</div>");

        NotificationRequest request = new NotificationRequest();
        request.setChannel(NotificationChannel.EMAIL);
        request.setTemplateCode("SIMPLE_EMAIL");
        request.setTemplateParameters(null);

        when(notificationTemplateService.findActiveByCode("SIMPLE_EMAIL", NotificationChannel.EMAIL)).thenReturn(template);
        when(thymeleafTemplateService.processTemplate("<div>Welcome</div>", null)).thenReturn("<div>Welcome</div>");
        when(thymeleafTemplateService.processTemplate("<div>Simple body without placeholders</div>", null)).thenReturn("<div>Simple body without placeholders</div>");

        notificationTemplateResolver.resolve(request);

        assertEquals("<div>Welcome</div>", request.getSubject());
        assertEquals("<div>Simple body without placeholders</div>", request.getBody());
        assertNull(request.getTemplateCode());
        verify(thymeleafTemplateService).processTemplate("<div>Welcome</div>", null);
        verify(thymeleafTemplateService).processTemplate("<div>Simple body without placeholders</div>", null);
    }

    @Test
    void resolve_ShouldResolveAllPlaceholders_WhenMultipleThymeleafPlaceholdersExist() {
        NotificationTemplate template = new NotificationTemplate();
        template.setSubject("Order [[${orderId}]] confirmed");
        template.setBody("<div>Dear [[${customerName}]], your order [[${orderId}]] for [[${amount}]] has been confirmed.</div>");

        Map<String, Object> parameters = Map.of(
                "orderId", "12345",
                "customerName", "John Doe",
                "amount", "$99.99"
        );

        NotificationRequest request = new NotificationRequest();
        request.setChannel(NotificationChannel.EMAIL);
        request.setTemplateCode("ORDER_EMAIL");
        request.setTemplateParameters(parameters);

        when(notificationTemplateService.findActiveByCode("ORDER_EMAIL", NotificationChannel.EMAIL)).thenReturn(template);
        when(thymeleafTemplateService.processTemplate(template.getSubject(), parameters)).thenReturn("Order 12345 confirmed");
        when(thymeleafTemplateService.processTemplate(template.getBody(), parameters)).thenReturn("<div>Dear John Doe, your order 12345 for $99.99 has been confirmed.</div>");

        notificationTemplateResolver.resolve(request);

        assertEquals("Order 12345 confirmed", request.getSubject());
        assertEquals("<div>Dear John Doe, your order 12345 for $99.99 has been confirmed.</div>", request.getBody());
        assertNull(request.getTemplateCode());
        verify(thymeleafTemplateService).processTemplate(template.getSubject(), parameters);
        verify(thymeleafTemplateService).processTemplate(template.getBody(), parameters);
    }

    @Test
    void resolve_ShouldUseThymeleafWithEmptyParameters_WhenTemplateIsHtml() {
        NotificationTemplate template = new NotificationTemplate();
        template.setSubject("Notification");
        template.setBody("<div>You have a new notification.</div>");

        NotificationRequest request = new NotificationRequest();
        request.setChannel(NotificationChannel.EMAIL);
        request.setTemplateCode("NOTIFICATION");
        request.setTemplateParameters(Map.of());

        when(notificationTemplateService.findActiveByCode("NOTIFICATION", NotificationChannel.EMAIL)).thenReturn(template);
        when(thymeleafTemplateService.processTemplate("<div>You have a new notification.</div>", Map.of())).thenReturn("<div>You have a new notification.</div>");

        notificationTemplateResolver.resolve(request);

        assertEquals("Notification", request.getSubject());
        assertEquals("<div>You have a new notification.</div>", request.getBody());
        assertNull(request.getTemplateCode());
        verify(notificationTemplateService).findActiveByCode("NOTIFICATION", NotificationChannel.EMAIL);
        verify(thymeleafTemplateService).processTemplate("<div>You have a new notification.</div>", Map.of());
    }

    @Test
    void resolve_ShouldSetSubjectAndBody_WhenRequestHasNoSubjectAndBody() {
        NotificationTemplate template = new NotificationTemplate();
        template.setSubject("Template Subject");
        template.setBody("<div>Template Body</div>");

        NotificationRequest request = new NotificationRequest();
        request.setChannel(NotificationChannel.EMAIL);
        request.setTemplateCode("WELCOME_EMAIL");
        request.setTemplateParameters(null);

        when(notificationTemplateService.findActiveByCode("WELCOME_EMAIL", NotificationChannel.EMAIL)).thenReturn(template);
        when(thymeleafTemplateService.processTemplate("<div>Template Body</div>", null)).thenReturn("<div>Template Body</div>");

        notificationTemplateResolver.resolve(request);

        assertEquals("Template Subject", request.getSubject());
        assertEquals("<div>Template Body</div>", request.getBody());
        assertNull(request.getTemplateCode());
    }

    @Test
    void resolve_ShouldResolveThymeleafLoopTemplate_WhenListOfObjectsProvided() {
        NotificationTemplate template = new NotificationTemplate();
        template.setSubject("[[${totalOrderCount}]] orders awaiting your approval");
        template.setBody("""
                <table>
                    <tr th:each="order : ${orders}">
                        <td th:text="${order.orderType}">Type</td>
                        <td th:text="${order.warehouseName}">Warehouse</td>
                        <td th:text="${order.orderAmount}">Amount</td>
                    </tr>
                </table>
                <p>Total: [[${totalAmount}]]</p>
                """);

        List<Map<String, String>> orders = List.of(
                Map.of("orderType", "Standard Order", "warehouseName", "Central", "orderAmount", "15,000"),
                Map.of("orderType", "Express Order", "warehouseName", "Branch", "orderAmount", "30,000")
        );

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("totalOrderCount", "2");
        parameters.put("orders", orders);
        parameters.put("totalAmount", "45,000");

        NotificationRequest request = new NotificationRequest();
        request.setChannel(NotificationChannel.EMAIL);
        request.setTemplateCode("ORDER_APPROVAL");
        request.setTemplateParameters(parameters);

        when(notificationTemplateService.findActiveByCode("ORDER_APPROVAL", NotificationChannel.EMAIL)).thenReturn(template);
        when(thymeleafTemplateService.processTemplate(template.getSubject(), parameters)).thenReturn("2 orders awaiting your approval");
        when(thymeleafTemplateService.processTemplate(template.getBody(), parameters)).thenReturn("<table>...</table>");

        notificationTemplateResolver.resolve(request);

        assertEquals("2 orders awaiting your approval", request.getSubject());
        assertEquals("<table>...</table>", request.getBody());
        assertNull(request.getTemplateCode());
        verify(thymeleafTemplateService).processTemplate(template.getSubject(), parameters);
        verify(thymeleafTemplateService).processTemplate(template.getBody(), parameters);
    }
}
