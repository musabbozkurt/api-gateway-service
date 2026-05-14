package com.mb.stockexchangeservice.utils;

import com.mb.stockexchangeservice.exception.BaseException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilsTest {

    private static final String JWT_SECRET = "testingSecretKeyTestingSecretKeyTestingSecretKeyTestingSecretKeyTestingSecretKeyTestingSecretKey";
    private static final int JWT_EXPIRATION_MS = 600000;

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", JWT_SECRET);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", JWT_EXPIRATION_MS);
    }

    private Authentication createAuthentication(String username, List<String> roles) {
        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
        UserDetails userDetails = new User(username, "", authorities);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @Nested
    @DisplayName("generateJwtToken")
    class GenerateJwtToken {

        @Test
        @DisplayName("should generate valid JWT token with username as subject")
        void generateJwtToken_ShouldContainUsername_WhenValidAuthenticationProvided() {
            // Arrange
            Authentication auth = createAuthentication("admin_user", List.of("ADMIN", "GET_PRODUCT"));

            // Act
            String token = jwtUtils.generateJwtToken(auth);

            // Assertions
            assertThat(token).isNotBlank();
            assertThat(jwtUtils.getUserNameFromJwtToken(token)).isEqualTo("admin_user");
        }

        @Test
        @DisplayName("should include roles in the JWT token")
        void generateJwtToken_ShouldIncludeRolesClaim_WhenUserHasRoles() {
            // Arrange
            List<String> roles = List.of("ADMIN", "GET_PRODUCT", "CREATE_PRODUCT");
            Authentication auth = createAuthentication("admin_user", roles);

            // Act
            String token = jwtUtils.generateJwtToken(auth);

            // Assertions
            var claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            @SuppressWarnings("unchecked")
            List<String> tokenRoles = claims.get(JwtUtils.ROLES_CLAIM, List.class);
            assertThat(tokenRoles).containsExactlyInAnyOrderElementsOf(roles);
        }

        @Test
        @DisplayName("should generate token with correct expiration")
        void generateJwtToken_ShouldHaveValidExpiration_WhenTokenGenerated() {
            // Arrange
            Authentication auth = createAuthentication("user1", List.of("GET_STOCK"));

            // Act
            String token = jwtUtils.generateJwtToken(auth);

            // Assertions
            var claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            assertThat(claims.getExpiration()).isAfter(new Date());
            assertThat(claims.getIssuedAt()).isBeforeOrEqualTo(new Date());
        }

        @Test
        @DisplayName("should throw BaseException when principal is null")
        void generateJwtToken_ShouldThrowBaseException_WhenPrincipalIsNull() {
            // Arrange
            Authentication auth = new UsernamePasswordAuthenticationToken(null, null);

            // Act
            // Assertions
            assertThatThrownBy(() -> jwtUtils.generateJwtToken(auth))
                    .isInstanceOf(BaseException.class);
        }
    }

    @Nested
    @DisplayName("getUserNameFromJwtToken")
    class GetUserNameFromJwtToken {

        @Test
        @DisplayName("should extract username from valid token")
        void getUserNameFromJwtToken_ShouldReturnUsername_WhenTokenIsValid() {
            // Arrange
            Authentication auth = createAuthentication("test_user", List.of("ADMIN"));
            String token = jwtUtils.generateJwtToken(auth);

            // Act
            String username = jwtUtils.getUserNameFromJwtToken(token);

            // Assertions
            assertThat(username).isEqualTo("test_user");
        }
    }

    @Nested
    @DisplayName("validateJwtToken")
    class ValidateJwtToken {

        @Test
        @DisplayName("should return true for valid token")
        void validateJwtToken_ShouldReturnTrue_WhenTokenIsValid() {
            // Arrange
            Authentication auth = createAuthentication("user1", List.of("ADMIN"));
            String token = jwtUtils.generateJwtToken(auth);

            // Act
            boolean result = jwtUtils.validateJwtToken(token);

            // Assertions
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should throw BaseException for malformed token")
        void validateJwtToken_ShouldThrowBaseException_WhenTokenIsMalformed() {
            // Arrange
            String malformedToken = "not.a.valid.token";

            // Act
            // Assertions
            assertThatThrownBy(() -> jwtUtils.validateJwtToken(malformedToken))
                    .isInstanceOf(BaseException.class);
        }

        @Test
        @DisplayName("should throw BaseException for expired token")
        void validateJwtToken_ShouldThrowBaseException_WhenTokenIsExpired() {
            // Arrange
            ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", -1000);
            Authentication auth = createAuthentication("user1", List.of("ADMIN"));
            String token = jwtUtils.generateJwtToken(auth);
            ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", JWT_EXPIRATION_MS);

            // Act
            // Assertions
            assertThatThrownBy(() -> jwtUtils.validateJwtToken(token))
                    .isInstanceOf(BaseException.class);
        }

        @Test
        @DisplayName("should throw BaseException for empty token")
        void validateJwtToken_ShouldThrowBaseException_WhenTokenIsEmpty() {
            // Arrange
            String emptyToken = "";

            // Act
            // Assertions
            assertThatThrownBy(() -> jwtUtils.validateJwtToken(emptyToken))
                    .isInstanceOf(BaseException.class);
        }

        @Test
        @DisplayName("should throw BaseException for token signed with different secret")
        void validateJwtToken_ShouldThrowBaseException_WhenTokenSignedWithDifferentSecret() {
            // Arrange
            String differentSecret = "differentSecretKeyDifferentSecretKeyDifferentSecretKeyDifferentSecretKeyDifferentSecretKey123";
            String token = Jwts.builder()
                    .subject("user1")
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + 600000))
                    .signWith(Keys.hmacShaKeyFor(differentSecret.getBytes(StandardCharsets.UTF_8)))
                    .compact();

            // Act
            // Assertions
            assertThatThrownBy(() -> jwtUtils.validateJwtToken(token))
                    .isInstanceOf(BaseException.class);
        }
    }
}
