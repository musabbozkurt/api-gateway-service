package com.mb.apigateway.context;

import lombok.Builder;

/**
 * Thread-local context holder for managing user context information across the application.
 * <p>
 * This class provides a thread-safe way to store and retrieve context information
 * such as username, client ID, and user ID. The context is stored per thread using
 * {@link InheritableThreadLocal}, which means child threads will inherit the context
 * from their parent thread.
 * </p>
 * <p>
 * Usage example:
 * <pre>
 * // Set context
 * Context context = Context.builder()
 *     .username("john.doe")
 *     .clientId("web-app")
 *     .userId(123L)
 *     .build();
 * ContextHolder.setContext(context);
 *
 * // Retrieve context
 * Context current = ContextHolder.getContext();
 *
 * // Clear context when done
 * ContextHolder.clear();
 * </pre>
 * </p>
 *
 * @see Context
 * @see InheritableThreadLocal
 */
public class ContextHolder {

    private static final ThreadLocal<Context> CONTEXT = new InheritableThreadLocal<>();

    public static Context getContext() {
        Context context = CONTEXT.get();
        return context == null ? Context.builder().build() : context;
    }

    public static void setContext(Context context) {
        CONTEXT.set(context);
    }

    public static void clear() {
        CONTEXT.remove();
    }

    @Builder
    public record Context(String username, String clientId, String sessionId) {
    }
}
