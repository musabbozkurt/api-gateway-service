package com.mb.paymentservice.exception;

import org.springframework.beans.TypeMismatchException;
import org.springframework.stereotype.Component;
import org.springframework.validation.ObjectError;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class SpringValidationWebExceptionMessageBuilder {

    public ErrorMessage errorMessage(ObjectError error, String defaultMessage) {
        String code = null;

        try {
            code = ConstraintViolations.getErrorCode(error.unwrap(ConstraintViolation.class));
        } catch (Exception ignored) {
        }

        if (code == null) {
            try {
                TypeMismatchException mismatchException = error.unwrap(TypeMismatchException.class);
                code = getErrorCode(mismatchException);
            } catch (Exception ignored) {
            }
        }

        if (code == null) {
            code = ErrorCode.VALIDATION_ERROR.name();
        }

        code = code.replace("{", "").replace("}", "");
        return new ErrorMessage(code, this.arguments(error), defaultMessage);
    }

    private List<Argument> arguments(ObjectError error) {
        try {
            ConstraintViolation<?> violation = (ConstraintViolation) error.unwrap(ConstraintViolation.class);
            return ConstraintViolations.getArguments(violation);
        } catch (Exception ex) {
            try {
                return getArguments(error.unwrap(TypeMismatchException.class));
            } catch (Exception exception) {
                return Collections.emptyList();
            }
        }
    }

    public List<Argument> getArguments(TypeMismatchException mismatchException) {
        List<Argument> arguments = new ArrayList();
        arguments.add(new Argument("property", getPropertyName(mismatchException)));
        arguments.add(new Argument("invalid", mismatchException.getValue()));
        Class<?> requiredType = mismatchException.getRequiredType();
        if (requiredType != null) {
            arguments.add(new Argument("expected", requiredType.getName()));
        }

        return arguments;
    }

    public String getErrorCode(TypeMismatchException mismatchException) {
        return String.format("%s.%s", ErrorCode.TYPE_MISMATCH, getPropertyName(mismatchException));
    }

    private String getPropertyName(TypeMismatchException mismatchException) {
        return mismatchException instanceof MethodArgumentTypeMismatchException ? ((MethodArgumentTypeMismatchException) mismatchException).getName() : mismatchException.getPropertyName();
    }
}
