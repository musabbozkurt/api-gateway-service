package com.mb.notificationservice.service;

import com.mb.notificationservice.service.impl.ThymeleafTemplateServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ThymeleafTemplateServiceTest {

    private ThymeleafTemplateService thymeleafTemplateService;

    @BeforeEach
    void setUp() {
        thymeleafTemplateService = new ThymeleafTemplateServiceImpl();
    }

    @Test
    void processTemplate_ShouldReturnNull_WhenTemplateIsNull() {
        String result = thymeleafTemplateService.processTemplate(null, Map.of("name", "Test"));
        assertNull(result);
    }

    @Test
    void processTemplate_ShouldResolveSimpleVariable_WhenVariableProvided() {
        String template = "<p th:text=\"${name}\">Default</p>";
        String result = thymeleafTemplateService.processTemplate(template, Map.of("name", "Ahmet"));

        assertTrue(result.contains("Ahmet"));
        assertFalse(result.contains("Default"));
    }

    @Test
    void processTemplate_ShouldIterateOverList_WhenListProvided() {
        String template = """
                <table>
                    <tr th:each="item : ${items}">
                        <td th:text="${item.name}">Name</td>
                        <td th:text="${item.value}">Value</td>
                    </tr>
                </table>
                """;

        List<Map<String, String>> items = List.of(
                Map.of("name", "Item 1", "value", "100"),
                Map.of("name", "Item 2", "value", "200")
        );

        String result = thymeleafTemplateService.processTemplate(template, Map.of("items", items));

        assertTrue(result.contains("Item 1"));
        assertTrue(result.contains("Item 2"));
        assertTrue(result.contains("100"));
        assertTrue(result.contains("200"));
    }

    @Test
    void processTemplate_ShouldHandleEmptyVariables_WhenNoVariablesProvided() {
        String template = "<p>Static content</p>";
        String result = thymeleafTemplateService.processTemplate(template, null);
        assertTrue(result.contains("Static content"));
    }
}
