package com.mb.notificationservice.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TokenUtils {

    private static final String BEARER = "Bearer ";
    private static final String AUTHORIZATION = "Authorization";
    private static final String USER_NAME = "user_name";
    private static final String CLIENT_ID = "client_id";
    private static final String USER_ID = "userId";
    private static final String ROLES = "authorities";
    private static final String ROLE_CONSTANT_NAMES = "roleConstantNames";
    private static final String USER_FULL_NAME = "userFullName";

    public static String getUsername() {
        JSONObject payload = getPayloadFromToken();
        if (Objects.nonNull(payload)) {
            return (String) payload.get(USER_NAME);
        }

        return null;
    }

    public static String getClientId() {
        JSONObject payload = getPayloadFromToken();
        if (Objects.nonNull(payload)) {
            return (String) payload.get(CLIENT_ID);
        }

        return null;
    }

    public static Long getUserId() {
        JSONObject payload = getPayloadFromToken();
        if (Objects.nonNull(payload)) {
            return Long.valueOf((Integer) payload.get(USER_ID));
        }

        return null;
    }

    public static List<String> getUserRoles() {
        return getRoles(ROLES);
    }

    public static List<String> getUserRoleConstantNames() {
        return getRoles(ROLE_CONSTANT_NAMES);
    }

    public static String getUserFullName() {
        JSONObject payload = getPayloadFromToken();
        if (Objects.nonNull(payload)) {
            return (String) payload.get(USER_FULL_NAME);
        }

        return null;
    }

    public static Object getClaimFromToken(String claim) {
        JSONObject payload = getPayloadFromToken();
        if (Objects.nonNull(payload)) {
            return payload.get(claim);
        }

        return null;
    }

    private static JSONObject getPayloadFromToken() {
        String accessToken = getToken();
        if (StringUtils.isBlank(accessToken)) {
            return null;
        }

        String[] parts = accessToken.split("\\.");
        return new JSONObject(decode(parts[1]));
    }

    private static String decode(String encodedString) {
        return new String(Base64.getUrlDecoder().decode(encodedString));
    }

    private static String getToken() {
        String authHeader = "";
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (Objects.nonNull(servletRequestAttributes)) {
            HttpServletRequest request = servletRequestAttributes.getRequest();
            if (StringUtils.isNotBlank(request.getHeader(AUTHORIZATION))) {
                authHeader = request.getHeader(AUTHORIZATION);
            }
        }

        return authHeader.replace(BEARER, "");
    }

    private static List<String> getRoles(String roles) {
        JSONObject payload = getPayloadFromToken();
        List<String> roleList = new ArrayList<>();
        if (Objects.nonNull(payload)) {
            JSONArray roleArray = (JSONArray) payload.get(roles);
            if (roleArray != null) {
                for (int i = 0; i < roleArray.length(); i++) {
                    roleList.add(roleArray.getString(i));
                }
            }
        }

        return roleList;
    }
}
