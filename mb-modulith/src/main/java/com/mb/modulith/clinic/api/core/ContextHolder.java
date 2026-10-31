package com.mb.modulith.clinic.api.core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ContextHolder {

    private static final ThreadLocal<Context> THREADLOCAL = new InheritableThreadLocal<>();

    public static Context get() {
        return Objects.isNull(THREADLOCAL.get()) ? Context.builder().build() : THREADLOCAL.get();
    }

    public static void set(Context context) {
        THREADLOCAL.set(context);
    }

    public static void clear() {
        THREADLOCAL.remove();
    }

}


