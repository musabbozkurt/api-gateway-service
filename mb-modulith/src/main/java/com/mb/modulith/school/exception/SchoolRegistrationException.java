package com.mb.modulith.school.exception;

import java.io.Serial;

public class SchoolRegistrationException extends IllegalStateException {

    @Serial
    private static final long serialVersionUID = 1L;

    public SchoolRegistrationException(String message) {
        super(message);
    }

    public SchoolRegistrationException(Throwable e) {
        super(e);
    }

}
