package com.mb.modulith.commerce.util;

import com.mb.modulith.commerce.exception.SharedErrorCode;
import com.mb.modulith.shared.exception.BaseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonUtilsTest {

    private JsonUtils jsonUtils;

    @BeforeEach
    void setUp() {
        // Arrange
        jsonUtils = new JsonUtils(new ObjectMapper());
    }

    @Test
    void serialize_ShouldReturnJson_WhenObjectIsValid() {
        // Act
        String json = jsonUtils.serialize(Map.of("key", "value"));

        // Assertions
        assertTrue(json.contains("key"));
        assertTrue(json.contains("value"));
    }

    @Test
    void serialize_ShouldThrowBaseException_WhenSerializationFails() {
        // Arrange
        class ExplodingBean {
            @SuppressWarnings("unused")
            public String getValue() {
                throw new IllegalStateException("serialization failed");
            }
        }

        ExplodingBean bean = new ExplodingBean();

        // Act
        BaseException ex = assertThrows(BaseException.class, () -> jsonUtils.serialize(bean));

        // Assertions
        assertEquals(SharedErrorCode.CANNOT_MAP_RESPONSE, ex.getErrorCode());
    }

    @Test
    void deserializeToClass_ShouldReturnNull_WhenContentIsNull() {
        // Arrange
        String content = null;

        // Act
        Map<?, ?> result = jsonUtils.deserialize(content, Map.class);

        // Assertions
        assertNull(result);
    }

    @Test
    void deserializeToClass_ShouldReturnContent_WhenTargetIsStringClass() {
        // Act
        String result = jsonUtils.deserialize("plain-text", String.class);

        // Assertions
        assertEquals("plain-text", result);
    }

    @Test
    void deserializeToClass_ShouldParseJson_WhenContentIsValid() {
        // Act
        Map<?, ?> map = jsonUtils.deserialize("{\"a\":1}", Map.class);

        // Assertions
        assertEquals(1, map.get("a"));
    }

    @Test
    void deserializeToClass_ShouldThrowBaseException_WhenJsonIsInvalid() {
        // Arrange
        String invalidJson = "{invalid";

        // Act
        BaseException ex = assertThrows(BaseException.class, () -> jsonUtils.deserialize(invalidJson, Map.class));

        // Assertions
        assertEquals(SharedErrorCode.CANNOT_MAP_RESPONSE, ex.getErrorCode());
    }

    @Test
    void deserializeTypeReference_ShouldReturnNull_WhenContentIsNull() {
        // Act
        List<String> result = jsonUtils.deserialize(null, new TypeReference<>() {
        });

        // Assertions
        assertNull(result);
    }

    @Test
    void deserializeTypeReference_ShouldReturnContent_WhenTargetIsString() {
        // Act
        String result = jsonUtils.deserialize("hello", new TypeReference<String>() {
        });

        // Assertions
        assertEquals("hello", result);
    }

    @Test
    void deserializeTypeReference_ShouldParseJson_WhenContentIsValid() {
        // Act
        List<String> list = jsonUtils.deserialize("[\"a\",\"b\"]", new TypeReference<>() {
        });

        // Assertions
        assertEquals(List.of("a", "b"), list);
    }

    @Test
    void deserializeTypeReference_ShouldThrowBaseException_WhenJsonIsInvalid() {
        // Arrange
        String invalidJson = "[";
        TypeReference<List<String>> listType = new TypeReference<>() {
        };

        // Act
        BaseException ex = assertThrows(BaseException.class, () -> jsonUtils.deserialize(invalidJson, listType));

        // Assertions
        assertEquals(SharedErrorCode.CANNOT_MAP_RESPONSE, ex.getErrorCode());
    }

    @Test
    void deserializeJsonNode_ShouldMapNode_WhenNodeIsValid() {
        // Arrange
        JsonNode node = new ObjectMapper().readTree("{\"name\":\"test\"}");

        // Act
        Map<?, ?> map = jsonUtils.deserialize(node, Map.class);

        // Assertions
        assertEquals("test", map.get("name"));
    }

    @Test
    void deserializeJsonNode_ShouldThrowBaseException_WhenMappingFails() {
        // Arrange
        JsonNode node = new ObjectMapper().readTree("{\"x\":1}");

        // Act
        BaseException ex = assertThrows(BaseException.class, () -> jsonUtils.deserialize(node, Integer.class));

        // Assertions
        assertEquals(SharedErrorCode.CANNOT_MAP_RESPONSE, ex.getErrorCode());
    }

    @Test
    void deserializeReflectiveType_ShouldParseJson_WhenTypeIsValid() {
        // Arrange
        Type type = new TypeReference<Map<String, Integer>>() {
        }.getType();

        // Act
        Map<String, Integer> map = jsonUtils.deserialize("{\"n\":2}", type);

        // Assertions
        assertEquals(2, map.get("n"));
    }

    @Test
    void deserializeReflectiveType_ShouldThrowBaseException_WhenJsonIsInvalid() {
        // Arrange
        Type type = new TypeReference<Map<String, Integer>>() {
        }.getType();
        String invalidJson = "{";

        // Act
        BaseException ex = assertThrows(BaseException.class, () -> jsonUtils.deserialize(invalidJson, type));

        // Assertions
        assertEquals(SharedErrorCode.CANNOT_MAP_RESPONSE, ex.getErrorCode());
    }

    @Test
    void deserializeTree_ShouldReturnJsonNode_WhenContentIsValid() {
        // Act
        JsonNode node = jsonUtils.deserialize("{\"k\":\"v\"}");

        // Assertions
        assertEquals("v", node.get("k").asString());
    }

    @Test
    void deserializeTree_ShouldThrowBaseException_WhenContentIsInvalid() {
        // Arrange
        String invalidJson = "{";

        // Act
        BaseException ex = assertThrows(BaseException.class, () -> jsonUtils.deserialize(invalidJson));

        // Assertions
        assertEquals(SharedErrorCode.CANNOT_MAP_RESPONSE, ex.getErrorCode());
    }

    @Test
    void convert_ShouldMapValues_WhenStructureMatches() {
        // Act
        Map<?, ?> converted = jsonUtils.convert(Map.of("id", 5), Map.class);

        // Assertions
        assertEquals(5, converted.get("id"));
    }
}
