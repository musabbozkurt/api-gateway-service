package com.mb.notificationservice.util;

import jakarta.servlet.http.HttpServletRequest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenUtilsTest {

    private static final String ROLES = "authorities";
    private static final String ROLE_CONSTANT_NAMES = "roleConstantNames";

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void getToken_ShouldReturnTokenWithoutBearerPrefix_WhenAuthorizationHeaderExistsInRequestContext() {
        // Arrange
        MockHttpServletRequest request = requestWithBearerToken(buildJwtToken(Map.of(TokenUtils.CLIENT_ID, "cli")));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Act
        String token = TokenUtils.getToken();

        // Assertions
        assertTrue(token.contains("."));
        assertFalse(token.startsWith("Bearer "));
    }

    @Test
    void getToken_ShouldReturnEmptyString_WhenRequestContextIsMissing() {
        // Arrange
        RequestContextHolder.resetRequestAttributes();

        // Act
        String token = TokenUtils.getToken();

        // Assertions
        assertEquals("", token);
    }

    @Test
    void getToken_ShouldReturnTokenWithoutBearerPrefix_WhenAuthorizationHeaderExistsInRequest() {
        // Arrange
        MockHttpServletRequest request = requestWithBearerToken(buildJwtToken(Map.of(TokenUtils.CLIENT_ID, "cli")));

        // Act
        String token = TokenUtils.getToken(request);

        // Assertions
        assertTrue(token.contains("."));
        assertFalse(token.startsWith("Bearer "));
    }

    @Test
    void getToken_ShouldReturnEmptyString_WhenAuthorizationHeaderMissingInRequest() {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();

        // Act
        String token = TokenUtils.getToken(request);

        // Assertions
        assertEquals("", token);
    }

    @Test
    void getToken_ShouldReturnHeaderAsIs_WhenAuthorizationHeaderDoesNotContainBearerPrefix() {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TokenUtils.AUTHORIZATION, "raw-token-without-prefix");

        // Act
        String token = TokenUtils.getToken(request);

        // Assertions
        assertEquals("raw-token-without-prefix", token);
    }

    @Test
    void getPayload_ShouldReturnDecodedPayload_WhenJwtTokenIsValid() throws JSONException {
        // Arrange
        MockHttpServletRequest request = requestWithBearerToken(buildJwtToken(Map.of(
                TokenUtils.CLIENT_ID, "client-a",
                TokenUtils.USERNAME, "user-a",
                TokenUtils.USER_ID, 101
        )));

        // Act
        JSONObject payload = TokenUtils.getPayload(request);

        // Assertions
        assertNotNull(payload);
        assertEquals("client-a", payload.getString(TokenUtils.CLIENT_ID));
        assertEquals("user-a", payload.getString(TokenUtils.USERNAME));
        assertEquals(101, payload.getInt(TokenUtils.USER_ID));
    }

    @Test
    void getPayload_ShouldReturnNull_WhenTokenIsOpaque() {
        // Arrange
        MockHttpServletRequest request = requestWithBearerToken("opaque-token");

        // Act
        JSONObject payload = TokenUtils.getPayload(request);

        // Assertions
        assertNull(payload);
    }

    @Test
    void getPayload_ShouldThrowJSONException_WhenDecodedPayloadIsNotJson() {
        // Arrange
        MockHttpServletRequest request = requestWithBearerToken("header.invalid-base64.signature");

        // Act
        JSONException exception = assertThrows(JSONException.class, () -> TokenUtils.getPayload(request));

        // Assertions
        assertNotNull(exception);
    }

    @Test
    void getClientId_ShouldReturnValue_WhenClaimExistsInRequestContextToken() {
        // Arrange
        MockHttpServletRequest request = requestWithBearerToken(buildJwtToken(Map.of(TokenUtils.CLIENT_ID, "client-ctx")));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Act
        String clientId = TokenUtils.getClientId();

        // Assertions
        assertEquals("client-ctx", clientId);
    }

    @Test
    void getClientId_ShouldReturnValue_WhenClaimExistsInRequestToken() {
        // Arrange
        MockHttpServletRequest request = requestWithBearerToken(buildJwtToken(Map.of(TokenUtils.CLIENT_ID, "client-req")));

        // Act
        String clientId = TokenUtils.getClientId(request);

        // Assertions
        assertEquals("client-req", clientId);
    }

    @Test
    void getClientId_ShouldReturnNull_WhenPayloadIsNull() {
        // Arrange
        JSONObject payload = null;

        // Act
        String clientId = TokenUtils.getClientId(payload);

        // Assertions
        assertNull(clientId);
    }

    @Test
    void getClientId_ShouldReturnNull_WhenRequestIsNull() {
        // Arrange
        HttpServletRequest request = null;

        // Act
        String clientId = TokenUtils.getClientId(request);

        // Assertions
        assertNull(clientId);
    }

    @Test
    void getClientId_ShouldReturnNull_WhenClaimDoesNotExistInPayload() {
        // Arrange
        JSONObject payload = new JSONObject(Map.of(TokenUtils.USERNAME, "user-only"));

        // Act
        String clientId = TokenUtils.getClientId(payload);

        // Assertions
        assertNull(clientId);
    }

    @Test
    void getClientId_ShouldReturnNull_WhenRequestContextTokenDoesNotContainClaim() {
        // Arrange
        MockHttpServletRequest request = requestWithBearerToken(buildJwtToken(Map.of(TokenUtils.USERNAME, "user-only")));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Act
        String clientId = TokenUtils.getClientId();

        // Assertions
        assertNull(clientId);
    }

    @Test
    void getUsername_ShouldReturnValue_WhenClaimExistsInRequestContextToken() {
        // Arrange
        MockHttpServletRequest request = requestWithBearerToken(buildJwtToken(Map.of(TokenUtils.USERNAME, "username-ctx")));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Act
        String username = TokenUtils.getUsername();

        // Assertions
        assertEquals("username-ctx", username);
    }

    @Test
    void getUsername_ShouldReturnValue_WhenClaimExistsInRequestToken() {
        // Arrange
        MockHttpServletRequest request = requestWithBearerToken(buildJwtToken(Map.of(TokenUtils.USERNAME, "username-req")));

        // Act
        String username = TokenUtils.getUsername(request);

        // Assertions
        assertEquals("username-req", username);
    }

    @Test
    void getUsername_ShouldReturnNull_WhenClaimDoesNotExistInPayload() {
        // Arrange
        JSONObject payload = new JSONObject(Map.of(TokenUtils.CLIENT_ID, "cli-only"));

        // Act
        String username = TokenUtils.getUsername(payload);

        // Assertions
        assertNull(username);
    }

    @Test
    void getUsername_ShouldReturnNull_WhenRequestIsNull() {
        // Arrange
        HttpServletRequest request = null;

        // Act
        String username = TokenUtils.getUsername(request);

        // Assertions
        assertNull(username);
    }

    @Test
    void getUsername_ShouldReturnNull_WhenPayloadIsNull() {
        // Arrange
        JSONObject payload = null;

        // Act
        String username = TokenUtils.getUsername(payload);

        // Assertions
        assertNull(username);
    }

    @Test
    void getUsername_ShouldReturnNull_WhenRequestContextTokenDoesNotContainClaim() {
        // Arrange
        MockHttpServletRequest request = requestWithBearerToken(buildJwtToken(Map.of(TokenUtils.CLIENT_ID, "client-only")));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Act
        String username = TokenUtils.getUsername();

        // Assertions
        assertNull(username);
    }

    @Test
    void getUserId_ShouldReturnLongValue_WhenClaimIsIntegerInPayload() {
        // Arrange
        JSONObject payload = new JSONObject(Map.of(TokenUtils.USER_ID, 55));

        // Act
        Long userId = TokenUtils.getUserId(payload);

        // Assertions
        assertEquals(55L, userId);
    }

    @Test
    void getUserId_ShouldReturnLongValue_WhenClaimIsLongInPayload() {
        // Arrange
        JSONObject payload = new JSONObject(Map.of(TokenUtils.USER_ID, 99L));

        // Act
        Long userId = TokenUtils.getUserId(payload);

        // Assertions
        assertEquals(99L, userId);
    }

    @Test
    void getUserId_ShouldReturnLongValue_WhenClaimIsNumericStringInPayload() {
        // Arrange
        JSONObject payload = new JSONObject(Map.of(TokenUtils.USER_ID, "1234"));

        // Act
        Long userId = TokenUtils.getUserId(payload);

        // Assertions
        assertEquals(1234L, userId);
    }

    @Test
    void getUserId_ShouldThrowNumberFormatException_WhenClaimIsNonNumericStringInPayload() {
        // Arrange
        JSONObject payload = new JSONObject(Map.of(TokenUtils.USER_ID, "abc"));

        // Act
        NumberFormatException exception = assertThrows(NumberFormatException.class, () -> TokenUtils.getUserId(payload));

        // Assertions
        assertNotNull(exception);
    }

    @Test
    void getUserId_ShouldReturnValue_WhenClaimExistsInRequestContextToken() {
        // Arrange
        MockHttpServletRequest request = requestWithBearerToken(buildJwtToken(Map.of(TokenUtils.USER_ID, 77)));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Act
        Long userId = TokenUtils.getUserId();

        // Assertions
        assertEquals(77L, userId);
    }

    @Test
    void getUserId_ShouldReturnNull_WhenPayloadDoesNotContainUserId() {
        // Arrange
        JSONObject payload = new JSONObject(Map.of(TokenUtils.CLIENT_ID, "x"));

        // Act
        Long userId = TokenUtils.getUserId(payload);

        // Assertions
        assertNull(userId);
    }

    @Test
    void getUserId_ShouldReturnNull_WhenClaimIsBlankStringInPayload() {
        // Arrange
        JSONObject payload = new JSONObject(Map.of(TokenUtils.USER_ID, "   "));

        // Act
        Long userId = TokenUtils.getUserId(payload);

        // Assertions
        assertNull(userId);
    }

    @Test
    void getUserId_ShouldReturnNull_WhenRequestContextTokenDoesNotContainClaim() {
        // Arrange
        MockHttpServletRequest request = requestWithBearerToken(buildJwtToken(Map.of(TokenUtils.CLIENT_ID, "client-only")));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Act
        Long userId = TokenUtils.getUserId();

        // Assertions
        assertNull(userId);
    }

    @Test
    void getUserRoles_ShouldReturnValues_WhenAuthoritiesClaimExists() {
        // Arrange
        MockHttpServletRequest request = requestWithBearerToken(buildJwtToken(Map.of(ROLES, List.of("ROLE_A", "ROLE_B"))));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Act
        List<String> roles = TokenUtils.getUserRoles();

        // Assertions
        assertEquals(2, roles.size());
        assertEquals("ROLE_A", roles.getFirst());
        assertEquals("ROLE_B", roles.get(1));
    }

    @Test
    void getUserRoleConstantNames_ShouldReturnValues_WhenRoleConstantNamesClaimExists() {
        // Arrange
        MockHttpServletRequest request = requestWithBearerToken(buildJwtToken(Map.of(ROLE_CONSTANT_NAMES, List.of("RCN_A", "RCN_B"))));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Act
        List<String> roleConstantNames = TokenUtils.getUserRoleConstantNames();

        // Assertions
        assertEquals(2, roleConstantNames.size());
        assertEquals("RCN_A", roleConstantNames.getFirst());
        assertEquals("RCN_B", roleConstantNames.get(1));
    }

    @Test
    void getUserFullName_ShouldReturnValue_WhenClaimExistsInRequestContextToken() {
        // Arrange
        MockHttpServletRequest request = requestWithBearerToken(buildJwtToken(Map.of("userFullName", "John Doe")));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Act
        String fullName = TokenUtils.getUserFullName();

        // Assertions
        assertEquals("John Doe", fullName);
    }

    @Test
    void getClaimFromToken_ShouldReturnClaimValue_WhenClaimExistsInRequestContextToken() {
        // Arrange
        MockHttpServletRequest request = requestWithBearerToken(buildJwtToken(Map.of("tenant", "TR")));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Act
        Object claim = TokenUtils.getClaimFromToken("tenant");

        // Assertions
        assertEquals("TR", claim);
    }

    @Test
    void getClaimFromToken_ShouldReturnNull_WhenClaimDoesNotExistInRequestContextToken() {
        // Arrange
        MockHttpServletRequest request = requestWithBearerToken(buildJwtToken(Map.of(TokenUtils.CLIENT_ID, "client-only")));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Act
        Object claim = TokenUtils.getClaimFromToken("tenant");

        // Assertions
        assertNull(claim);
    }

    @Test
    void getUserRoles_ShouldReturnEmptyList_WhenAuthoritiesClaimDoesNotExist() {
        // Arrange
        MockHttpServletRequest request = requestWithBearerToken(buildJwtToken(Map.of(TokenUtils.CLIENT_ID, "client-only")));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Act
        List<String> roles = TokenUtils.getUserRoles();

        // Assertions
        assertTrue(roles.isEmpty());
    }

    @Test
    void getUserRoleConstantNames_ShouldReturnEmptyList_WhenRoleConstantNamesClaimDoesNotExist() {
        // Arrange
        MockHttpServletRequest request = requestWithBearerToken(buildJwtToken(Map.of(TokenUtils.CLIENT_ID, "client-only")));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Act
        List<String> roleConstantNames = TokenUtils.getUserRoleConstantNames();

        // Assertions
        assertTrue(roleConstantNames.isEmpty());
    }

    @Test
    void getUserFullName_ShouldReturnNull_WhenClaimDoesNotExistInRequestContextToken() {
        // Arrange
        MockHttpServletRequest request = requestWithBearerToken(buildJwtToken(Map.of(TokenUtils.CLIENT_ID, "client-only")));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Act
        String fullName = TokenUtils.getUserFullName();

        // Assertions
        assertNull(fullName);
    }

    @Test
    void getPayload_ShouldReturnNull_WhenRequestIsNull() {
        // Arrange
        HttpServletRequest request = null;

        // Act
        JSONObject payload = TokenUtils.getPayload(request);

        // Assertions
        assertNull(payload);
    }

    @Test
    void getPayload_ShouldReturnNull_WhenAuthorizationHeaderIsBlank() {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TokenUtils.AUTHORIZATION, "   ");

        // Act
        JSONObject payload = TokenUtils.getPayload(request);

        // Assertions
        assertNull(payload);
    }

    @Test
    void getClientId_ShouldReturnNull_WhenClaimIsJsonNullInPayload() throws JSONException {
        // Arrange
        JSONObject payload = new JSONObject();
        payload.put(TokenUtils.CLIENT_ID, JSONObject.NULL);

        // Act
        String clientId = TokenUtils.getClientId(payload);

        // Assertions
        assertNull(clientId);
    }

    @Test
    void getUsername_ShouldReturnNull_WhenClaimIsJsonNullInPayload() throws JSONException {
        // Arrange
        JSONObject payload = new JSONObject();
        payload.put(TokenUtils.USERNAME, JSONObject.NULL);

        // Act
        String username = TokenUtils.getUsername(payload);

        // Assertions
        assertNull(username);
    }

    @Test
    void getUserId_ShouldReturnNull_WhenClaimIsJsonNullInPayload() throws JSONException {
        // Arrange
        JSONObject payload = new JSONObject();
        payload.put(TokenUtils.USER_ID, JSONObject.NULL);

        // Act
        Long userId = TokenUtils.getUserId(payload);

        // Assertions
        assertNull(userId);
    }

    @Test
    void getClientId_ShouldReturnNull_WhenRequestContextIsMissing() {
        // Arrange
        RequestContextHolder.resetRequestAttributes();

        // Act
        String clientId = TokenUtils.getClientId();

        // Assertions
        assertNull(clientId);
    }

    @Test
    void getUsername_ShouldReturnNull_WhenRequestContextIsMissing() {
        // Arrange
        RequestContextHolder.resetRequestAttributes();

        // Act
        String username = TokenUtils.getUsername();

        // Assertions
        assertNull(username);
    }

    @Test
    void getUserId_ShouldReturnNull_WhenRequestContextIsMissing() {
        // Arrange
        RequestContextHolder.resetRequestAttributes();

        // Act
        Long userId = TokenUtils.getUserId();

        // Assertions
        assertNull(userId);
    }

    @Test
    void getClaimFromToken_ShouldReturnNull_WhenRequestContextIsMissing() {
        // Arrange
        RequestContextHolder.resetRequestAttributes();

        // Act
        Object claim = TokenUtils.getClaimFromToken("tenant");

        // Assertions
        assertNull(claim);
    }

    @Test
    void getUserRoles_ShouldReturnEmptyList_WhenRequestContextIsMissing() {
        // Arrange
        RequestContextHolder.resetRequestAttributes();

        // Act
        List<String> roles = TokenUtils.getUserRoles();

        // Assertions
        assertTrue(roles.isEmpty());
    }

    @Test
    void getUserRoleConstantNames_ShouldReturnEmptyList_WhenRequestContextIsMissing() {
        // Arrange
        RequestContextHolder.resetRequestAttributes();

        // Act
        List<String> roleConstantNames = TokenUtils.getUserRoleConstantNames();

        // Assertions
        assertTrue(roleConstantNames.isEmpty());
    }

    @Test
    void getUserRoles_ShouldReturnEmptyList_WhenAuthoritiesClaimIsEmptyArray() {
        // Arrange
        MockHttpServletRequest request = requestWithBearerToken(buildJwtToken(Map.of(ROLES, List.of())));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Act
        List<String> roles = TokenUtils.getUserRoles();

        // Assertions
        assertTrue(roles.isEmpty());
    }

    @Test
    void getUserRoleConstantNames_ShouldReturnEmptyList_WhenRoleConstantNamesClaimIsEmptyArray() {
        // Arrange
        MockHttpServletRequest request = requestWithBearerToken(buildJwtToken(Map.of(ROLE_CONSTANT_NAMES, List.of())));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Act
        List<String> roleConstantNames = TokenUtils.getUserRoleConstantNames();

        // Assertions
        assertTrue(roleConstantNames.isEmpty());
    }

    @Test
    void getUserId_ShouldReturnLongValue_WhenClaimIsNumericStringInRequestToken() {
        // Arrange
        MockHttpServletRequest request = requestWithBearerToken(buildJwtToken(Map.of(TokenUtils.USER_ID, "456")));

        // Act
        Long userId = TokenUtils.getUserId(request);

        // Assertions
        assertEquals(456L, userId);
    }

    @Test
    void getUserId_ShouldReturnNull_WhenRequestTokenHasLessThanTwoParts() {
        // Arrange
        MockHttpServletRequest request = requestWithBearerToken("opaque-token");

        // Act
        Long userId = TokenUtils.getUserId(request);

        // Assertions
        assertNull(userId);
    }

    @Test
    void getClientId_ShouldReturnNull_WhenRequestContextTokenHasLessThanTwoParts() {
        // Arrange
        MockHttpServletRequest request = requestWithBearerToken("opaque-token");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Act
        String clientId = TokenUtils.getClientId();

        // Assertions
        assertNull(clientId);
    }

    @Test
    void getToken_ShouldReturnEmptyString_WhenAuthorizationHeaderIsBlankInRequestContext() {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TokenUtils.AUTHORIZATION, "   ");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Act
        String token = TokenUtils.getToken();

        // Assertions
        assertEquals("", token);
    }

    @Test
    void getUserRoles_ShouldReturnEmptyList_WhenAuthoritiesClaimIsJsonNull() throws JSONException {
        // Arrange
        JSONObject claims = new JSONObject();
        claims.put(ROLES, JSONObject.NULL);
        MockHttpServletRequest request = requestWithBearerToken(buildJwtToken(claims));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Act
        List<String> roles = TokenUtils.getUserRoles();

        // Assertions
        assertTrue(roles.isEmpty());
    }

    @Test
    void getUserRoleConstantNames_ShouldReturnEmptyList_WhenRoleConstantNamesClaimIsJsonNull() throws JSONException {
        // Arrange
        JSONObject claims = new JSONObject();
        claims.put(ROLE_CONSTANT_NAMES, JSONObject.NULL);
        MockHttpServletRequest request = requestWithBearerToken(buildJwtToken(claims));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Act
        List<String> roleConstantNames = TokenUtils.getUserRoleConstantNames();

        // Assertions
        assertTrue(roleConstantNames.isEmpty());
    }

    @Test
    void getUserFullName_ShouldReturnEmptyString_WhenClaimIsEmptyStringInRequestContextToken() {
        // Arrange
        MockHttpServletRequest request = requestWithBearerToken(buildJwtToken(Map.of("userFullName", "")));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Act
        String fullName = TokenUtils.getUserFullName();

        // Assertions
        assertEquals("", fullName);
    }

    @Test
    void getClaimFromToken_ShouldReturnNumericClaim_WhenClaimExistsAsNumberInRequestContextToken() {
        // Arrange
        MockHttpServletRequest request = requestWithBearerToken(buildJwtToken(Map.of("tenantId", 42)));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Act
        Object claim = TokenUtils.getClaimFromToken("tenantId");

        // Assertions
        assertEquals(42, claim);
    }

    @Test
    void getUserId_ShouldReturnLongValue_WhenClaimIsNumericStringInRequestContextToken() {
        // Arrange
        MockHttpServletRequest request = requestWithBearerToken(buildJwtToken(Map.of(TokenUtils.USER_ID, "789")));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Act
        Long userId = TokenUtils.getUserId();

        // Assertions
        assertEquals(789L, userId);
    }

    @Test
    void getUserId_ShouldThrowNumberFormatException_WhenClaimIsNonNumericStringInRequestContextToken() {
        // Arrange
        MockHttpServletRequest request = requestWithBearerToken(buildJwtToken(Map.of(TokenUtils.USER_ID, "not-a-number")));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Act
        NumberFormatException exception = assertThrows(NumberFormatException.class, TokenUtils::getUserId);

        // Assertions
        assertNotNull(exception);
    }

    private MockHttpServletRequest requestWithBearerToken(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TokenUtils.AUTHORIZATION, "Bearer " + token);
        return request;
    }

    private String buildJwtToken(Map<String, Object> claims) {
        String headerJson = """
                {
                    "alg":"none",
                    "typ":"JWT"
                }
                """;
        String payloadJson = new JSONObject(claims).toString();
        String header = Base64.getUrlEncoder().withoutPadding().encodeToString(headerJson.getBytes(StandardCharsets.UTF_8));
        String payload = Base64.getUrlEncoder().withoutPadding().encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        return header + "." + payload + ".signature";
    }

    private String buildJwtToken(JSONObject claims) {
        String headerJson = """
                {
                    "alg":"none",
                    "typ":"JWT"
                }
                """;
        String payloadJson = claims.toString();
        String header = Base64.getUrlEncoder().withoutPadding().encodeToString(headerJson.getBytes(StandardCharsets.UTF_8));
        String payload = Base64.getUrlEncoder().withoutPadding().encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        return header + "." + payload + ".signature";
    }
}
