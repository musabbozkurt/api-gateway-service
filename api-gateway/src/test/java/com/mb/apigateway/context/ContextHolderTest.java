package com.mb.apigateway.context;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ContextHolderTest {

    @Test
    void getContext_ShouldReturnDefaultContext_WhenNoContextSet() {
        // Arrange
        ContextHolder.clear();

        // Act
        ContextHolder.Context context = ContextHolder.getContext();

        // Assertions
        Assertions.assertNull(context.username());
        Assertions.assertNull(context.clientId());
        Assertions.assertNull(context.sessionId());
    }

    @Test
    void setContext_ShouldStoreAndRetrieveContext_WhenContextIsSet() {
        // Arrange
        ContextHolder.Context expectedContext = ContextHolder.Context.builder()
                .username("john.doe")
                .clientId("web-app")
                .sessionId("abc123")
                .build();

        // Act
        ContextHolder.setContext(expectedContext);
        ContextHolder.Context actualContext = ContextHolder.getContext();

        // Assertions
        Assertions.assertEquals(expectedContext, actualContext);
    }

    @Test
    void update_ShouldUpdateContextWithNewUsername_WhenContextIsSetAndUsernameIsUpdated() {
        // Arrange
        ContextHolder.Context initialContext = ContextHolder.Context.builder()
                .username("john.doe")
                .clientId("web-app")
                .sessionId("abc123")
                .build();
        ContextHolder.setContext(initialContext);

        // Act
        ContextHolder.Context updatedContext = ContextHolder.getContext().toBuilder().username("jane.doe").build();

        // Assertions
        Assertions.assertEquals("jane.doe", updatedContext.username());
        Assertions.assertEquals("web-app", updatedContext.clientId());
        Assertions.assertEquals("abc123", updatedContext.sessionId());
    }

    @Test
    void update_ShouldUpdateContextWithNewSessionId_WhenContextIsSetAndSessionIdIsUpdated() {
        // Arrange
        ContextHolder.Context initialContext = ContextHolder.Context.builder()
                .username("john.doe")
                .clientId("web-app")
                .sessionId("abc123")
                .build();
        ContextHolder.setContext(initialContext);

        // Act
        ContextHolder.Context updatedContext = ContextHolder.getContext().toBuilder().sessionId("def456").build();

        // Assertions
        Assertions.assertEquals("john.doe", updatedContext.username());
        Assertions.assertEquals("web-app", updatedContext.clientId());
        Assertions.assertEquals("def456", updatedContext.sessionId());
    }

    @Test
    void update_ShouldUpdateContextWithNewClientId_WhenContextIsSetAndClientIdIsUpdated() {
        // Arrange
        ContextHolder.Context initialContext = ContextHolder.Context.builder()
                .username("john.doe")
                .clientId("web-app")
                .sessionId("abc123")
                .build();
        ContextHolder.setContext(initialContext);

        // Act
        ContextHolder.Context updatedContext = ContextHolder.getContext().toBuilder().clientId("mobile-app").build();

        // Assertions
        Assertions.assertEquals("john.doe", updatedContext.username());
        Assertions.assertEquals("mobile-app", updatedContext.clientId());
        Assertions.assertEquals("abc123", updatedContext.sessionId());
    }

    @Test
    void clear_ShouldRemoveContext_WhenContextIsSetAndContextIsCleared() {
        // Arrange
        ContextHolder.Context initialContext = ContextHolder.Context.builder()
                .username("john.doe")
                .clientId("web-app")
                .sessionId("abc123")
                .build();
        ContextHolder.setContext(initialContext);

        // Act
        ContextHolder.clear();
        ContextHolder.Context retrievedContext = ContextHolder.getContext();

        // Assertions
        Assertions.assertNull(retrievedContext.username());
        Assertions.assertNull(retrievedContext.clientId());
        Assertions.assertNull(retrievedContext.sessionId());
    }
}
