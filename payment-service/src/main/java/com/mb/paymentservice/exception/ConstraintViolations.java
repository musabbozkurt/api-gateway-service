package com.mb.paymentservice.exception;

import javax.validation.ConstraintViolation;
import javax.validation.constraints.Digits;
import javax.validation.constraints.*;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ConstraintViolations {
    private static final List<String> DEFAULT_ERROR_CODES_PREFIX = Arrays.asList("{javax.validation.", "{org.hibernate.validator");
    private static final Map<Class<? extends Annotation>, String> ERROR_CODE_MAPPING = initErrorCodeMapping();
    private static final Collection<String> IGNORE_ATTRIBUTES = Arrays.asList("groups", "payload", "message");

    static List<Argument> getArguments(ConstraintViolation<?> violation) {
        List<Argument> args = violation.getConstraintDescriptor().getAttributes().entrySet().stream()
                .filter(e -> !IGNORE_ATTRIBUTES.contains(e.getKey())).sorted(Map.Entry.comparingByKey())
                .map(e -> new Argument(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
        args.add(new Argument("invalid", violation.getInvalidValue()));
        args.add(new Argument("property", violation.getPropertyPath().toString()));
        return args;
    }

    static String getErrorCode(ConstraintViolation<?> violation) {
        String code = violation.getMessageTemplate();
        boolean shouldGenerateDefaultErrorCode = true;

        if (code != null && !code.trim().isEmpty()) {
            Stream<String> errorCodes = DEFAULT_ERROR_CODES_PREFIX.stream();

            if (errorCodes.noneMatch(code::startsWith)) {
                shouldGenerateDefaultErrorCode = false;
            }
        }

        if (shouldGenerateDefaultErrorCode) {
            String prefix = violation.getPropertyPath().toString();
            Class<? extends Annotation> annotation = violation.getConstraintDescriptor().getAnnotation().annotationType();
            String suffix = ERROR_CODE_MAPPING.getOrDefault(annotation, annotation.getSimpleName());
            return prefix + "." + suffix;
        } else {
            return code.replace("{", "").replace("}", "");
        }
    }

    private static Map<Class<? extends Annotation>, String> initErrorCodeMapping() {
        Map<Class<? extends Annotation>, String> codes = new HashMap<>();
        codes.put(AssertFalse.class, "shouldBeFalse");
        codes.put(AssertTrue.class, "shouldBeTrue");
        codes.put(DecimalMax.class, "exceedsMax");
        codes.put(DecimalMin.class, "lessThanMin");
        codes.put(Digits.class, "tooManyDigits");
        codes.put(Email.class, "invalidEmail");
        codes.put(Future.class, "shouldBeInFuture");
        codes.put(FutureOrPresent.class, "shouldBeInFutureOrPresent");
        codes.put(Max.class, "exceedsMax");
        codes.put(Min.class, "lessThanMin");
        codes.put(Negative.class, "shouldBeNegative");
        codes.put(NegativeOrZero.class, "shouldBeNegativeOrZero");
        codes.put(NotBlank.class, "shouldNotBeBlank");
        codes.put(NotEmpty.class, "shouldNotBeEmpty");
        codes.put(NotNull.class, "isRequired");
        codes.put(Null.class, "shouldBeMissing");
        codes.put(Past.class, "shouldBeInPast");
        codes.put(PastOrPresent.class, "shouldBeInPastOrPresent");
        codes.put(Pattern.class, "invalidPattern");
        codes.put(Positive.class, "shouldBePositive");
        codes.put(PositiveOrZero.class, "shouldBePositiveOrZero");
        codes.put(Size.class, "invalidSize");
        return codes;
    }
}
