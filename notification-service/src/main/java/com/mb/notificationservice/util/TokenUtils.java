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

    public static final String AUTHORIZATION = "Authorization";
    public static final String USERNAME = "user_name";
    public static final String CLIENT_ID = "client_id";
    public static final String USER_ID = "userId";

    private static final String BEARER = "Bearer ";
    private static final String ROLES = "authorities";
    private static final String ROLE_CONSTANT_NAMES = "roleConstantNames";
    private static final String USER_FULL_NAME = "userFullName";

    // -------------------------------------------------------------------------
    // No-arg methods (rely on RequestContextHolder — safe to use in
    // service/controller layer where request context is already bound)
    // -------------------------------------------------------------------------

    public static String getClientId() {
        return getClientId(getPayloadFromToken());
    }

    public static String getUsername() {
        return getUsername(getPayloadFromToken());
    }

    public static Long getUserId() {
        return getUserId(getPayloadFromToken());
    }

    public static List<String> getUserRoles() {
        return getRoles(ROLES);
    }

    public static List<String> getUserRoleConstantNames() {
        return getRoles(ROLE_CONSTANT_NAMES);
    }

    public static String getUserFullName() {
        JSONObject payload = getPayloadFromToken();
        if (Objects.nonNull(payload) && payload.has(USER_FULL_NAME) && !payload.isNull(USER_FULL_NAME)) {
            return payload.getString(USER_FULL_NAME);
        }
        return null;
    }

    public static Object getClaimFromToken(String claim) {
        JSONObject payload = getPayloadFromToken();
        if (Objects.nonNull(payload) && payload.has(claim) && !payload.isNull(claim)) {
            return payload.get(claim);
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // HttpServletRequest-based methods (bypass RequestContextHolder — safe to
    // use anywhere, including high-precedence filters before request context binding)
    // -------------------------------------------------------------------------

    public static String getClientId(HttpServletRequest request) {
        return getClientId(getPayloadFromToken(request));
    }

    public static String getUsername(HttpServletRequest request) {
        return getUsername(getPayloadFromToken(request));
    }

    public static Long getUserId(HttpServletRequest request) {
        return getUserId(getPayloadFromToken(request));
    }

    // -------------------------------------------------------------------------
    // Payload-based methods (parse ONCE, then call these multiple times)
    // Use getPayload(request) to obtain the payload, then pass it here.
    // This avoids repeated Base64/JSON parsing when multiple claims are needed.
    //
    // Example:
    //   JSONObject payload = TokenUtils.getPayload(request);
    //   String clientId = TokenUtils.getClientId(payload);
    //   String username = TokenUtils.getUsername(payload);
    //   Long userId    = TokenUtils.getUserId(payload);
    // -------------------------------------------------------------------------

    /**
     * Parses the JWT payload from the request a single time.
     * Returns {@code null} for opaque tokens or when the Authorization header is absent.
     * Pass the result to {@link #getClientId(JSONObject)}, {@link #getUsername(JSONObject)},
     * {@link #getUserId(JSONObject)} to avoid repeated parsing.
     */
    public static JSONObject getPayload(HttpServletRequest request) {
        return getPayloadFromToken(request);
    }

    public static String getClientId(JSONObject payload) {
        if (Objects.nonNull(payload) && payload.has(CLIENT_ID) && !payload.isNull(CLIENT_ID)) {
            return payload.getString(CLIENT_ID);
        }
        return null;
    }

    public static String getUsername(JSONObject payload) {
        if (Objects.nonNull(payload) && payload.has(USERNAME) && !payload.isNull(USERNAME)) {
            return payload.getString(USERNAME);
        }
        return null;
    }

    /**
     * Type-resilient userId extractor. Handles Integer, Long, and String
     * since different SSO token formats may encode userId differently.
     */
    public static Long getUserId(JSONObject payload) {
        if (Objects.nonNull(payload) && payload.has(USER_ID) && !payload.isNull(USER_ID)) {
            Object userId = payload.get(USER_ID);
            if (userId instanceof Number number) {
                return number.longValue();
            }
            if (userId instanceof String userIdString && StringUtils.isNotBlank(userIdString)) {
                return Long.valueOf(userIdString);
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Token accessors
    // -------------------------------------------------------------------------

    public static String getToken() {
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

    public static String getToken(HttpServletRequest request) {
        if (Objects.nonNull(request) && StringUtils.isNotBlank(request.getHeader(AUTHORIZATION))) {
            return request.getHeader(AUTHORIZATION).replace(BEARER, "");
        }
        return "";
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private static JSONObject getPayloadFromToken() {
        String accessToken = getToken();
        if (StringUtils.isBlank(accessToken)) {
            return null;
        }
        String[] parts = accessToken.split("\\.");
        if (parts.length < 2) {
            return null;
        }

        return new JSONObject(decode(parts[1]));
    }

    private static JSONObject getPayloadFromToken(HttpServletRequest request) {
        String accessToken = getToken(request);
        if (StringUtils.isBlank(accessToken)) {
            return null;
        }
        String[] parts = accessToken.split("\\.");
        if (parts.length < 2) {
            return null;
        }

        return new JSONObject(decode(parts[1]));
    }

    private static String decode(String encodedString) {
        return new String(Base64.getUrlDecoder().decode(encodedString));
    }

    private static List<String> getRoles(String roles) {
        JSONObject payload = getPayloadFromToken();
        List<String> roleList = new ArrayList<>();
        if (Objects.nonNull(payload) && payload.has(roles) && !payload.isNull(roles)) {
            JSONArray roleArray = payload.getJSONArray(roles);
            for (int i = 0; i < roleArray.length(); i++) {
                roleList.add(roleArray.getString(i));
            }
        }

        return roleList;
    }
}
