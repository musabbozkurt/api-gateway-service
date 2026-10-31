package com.mb.modulith.commerce.util;

import com.mb.modulith.commerce.exception.SharedErrorCode;
import com.mb.modulith.shared.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Type;

@Component
@RequiredArgsConstructor
public class JsonUtils {

    private final ObjectMapper mapper;

    public String serialize(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (Exception _) {
            throw new BaseException(SharedErrorCode.CANNOT_MAP_RESPONSE);
        }
    }

    public <T> T deserialize(String content, Class<T> clazz) {
        if (content == null) {
            return null;
        }
        if (String.class.equals(clazz)) {
            return clazz.cast(content);
        }
        try {
            return mapper.readValue(content, clazz);

        } catch (Exception _) {
            throw new BaseException(SharedErrorCode.CANNOT_MAP_RESPONSE);
        }
    }

    public <T> T deserialize(String content, TypeReference<T> typeReference) {
        if (content == null) {
            return null;
        }
        if (typeReference.getType().equals(String.class)) {
            return castStringValue(content);
        }
        try {
            return mapper.readValue(content, typeReference);

        } catch (Exception _) {
            throw new BaseException(SharedErrorCode.CANNOT_MAP_RESPONSE);
        }
    }

    public <T> T deserialize(JsonNode node, Class<T> clazz) {
        try {
            return mapper.treeToValue(node, clazz);
        } catch (Exception _) {
            throw new BaseException(SharedErrorCode.CANNOT_MAP_RESPONSE);
        }
    }

    public <T> T deserialize(String content, Type type) {
        try {
            return mapper.readValue(content, mapper.getTypeFactory().constructType(type));
        } catch (Exception _) {
            throw new BaseException(SharedErrorCode.CANNOT_MAP_RESPONSE);
        }
    }

    public JsonNode deserialize(String content) {
        try {
            return mapper.readTree(content);
        } catch (Exception _) {
            throw new BaseException(SharedErrorCode.CANNOT_MAP_RESPONSE);
        }
    }

    public <T> T convert(Object object, Class<T> clazz) {
        return mapper.convertValue(object, clazz);
    }

    @SuppressWarnings("unchecked")
    private <T> T castStringValue(String content) {
        return (T) content;
    }
}
