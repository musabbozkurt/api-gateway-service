package com.mb.paymentservice.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeType;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingMatrixVariableException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.MatrixVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.NotAcceptableStatusException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class RestResponseExceptionHandler {

    private static final String EXPECTED = "expected";
    private static final String TYPES = "types";

    private final ExceptionMessageTemplateParser templateParser;
    private final SpringValidationWebExceptionMessageBuilder messageBuilder;

    @ResponseBody
    @ExceptionHandler(PaymentServiceException.class)
    public ResponseEntity<?> handleTypeMismatchException(PaymentServiceException ex) {
        log.error("PaymentServiceException occurred. handleTypeMismatchException - ex: {}.", ExceptionUtils.getStackTrace(ex));
        return new ResponseEntity<>(new LocalizedErrorResponse(ex.getErrorCode().name(), ex.getMessage()), ex.getErrorCode().getHttpStatus());
    }

    @ResponseBody
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException ex) {
        log.error("ConstraintViolationException occurred. handleConstraintViolationException - ex: {}.", ExceptionUtils.getStackTrace(ex));

        HttpStatus httpStatus = HttpStatus.NOT_ACCEPTABLE;
        AtomicReference<String> rawMessage = new AtomicReference<>();
        AtomicReference<String> errorCode = new AtomicReference<>();
        ex.getConstraintViolations().forEach(constraintViolation -> {
            rawMessage.set(constraintViolation.getMessage());
            errorCode.set(ex.getMessage());
        });
        String formattedMessage = templateParser.interpolate(new ErrorMessage(errorCode.get(), null, rawMessage.get()));

        return new ResponseEntity<>(new LocalizedErrorResponse(errorCode.get(), formattedMessage), httpStatus);
    }

    @ResponseBody
    @ExceptionHandler(ConversionFailedException.class)
    public ResponseEntity<?> handleConversionFailedException(ConversionFailedException ex) {
        log.error("ConversionFailedException occurred. handleConversionFailedException - ex: {}.", ExceptionUtils.getStackTrace(ex));

        String errorCode = ErrorCode.CONVERSION_FAILED.name();
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        String formattedMessage = templateParser.interpolate(new ErrorMessage(errorCode, null, ex.getMessage()));

        return new ResponseEntity<>(new LocalizedErrorResponse(errorCode, formattedMessage), httpStatus);
    }


    @ResponseBody
    @ExceptionHandler({MissingRequestHeaderException.class,
            MissingRequestCookieException.class,
            MissingMatrixVariableException.class})
    public ResponseEntity<?> handleMissingRequestParametersException(Throwable ex) {
        log.error("{} occurred. handleMissingRequestParametersException - ex: {}.", ex.getClass().getSimpleName(), ExceptionUtils.getStackTrace(ex));

        List<Argument> arguments = new ArrayList<>();
        ErrorCode errorCode = ErrorCode.UNKNOWN_ERROR;
        HttpStatus httpStatus = ErrorCode.UNKNOWN_ERROR.getHttpStatus();

        switch (ex) {
            case MissingRequestHeaderException headerException -> {
                errorCode = ErrorCode.MISSING_HEADER;
                httpStatus = ErrorCode.MISSING_HEADER.getHttpStatus();
                arguments.add(new Argument("name", headerException.getHeaderName()));
                arguments.add(new Argument(EXPECTED, headerException.getParameter()));
            }
            case MissingRequestCookieException cookieException -> {
                errorCode = ErrorCode.MISSING_COOKIE;
                httpStatus = ErrorCode.MISSING_COOKIE.getHttpStatus();
                arguments.add(new Argument("name", cookieException.getCookieName()));
                arguments.add(new Argument(EXPECTED, cookieException.getParameter()));
            }
            case MissingMatrixVariableException variableException -> {
                errorCode = ErrorCode.MISSING_MATRIX_VARIABLE;
                httpStatus = ErrorCode.MISSING_MATRIX_VARIABLE.getHttpStatus();
                arguments.add(new Argument("name", variableException.getVariableName()));
                arguments.add(new Argument(EXPECTED, variableException.getParameter()));
            }
        }
        String formattedMessage = templateParser.interpolate(new ErrorMessage(errorCode.name(), arguments, ex.getMessage()));

        return new ResponseEntity<>(new LocalizedErrorResponse(errorCode.name(), formattedMessage), httpStatus);

    }

    @ResponseBody
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<?> handleMissingMatrixVariableException(MultipartException ex) {
        log.error("MultipartException occurred. handleMissingMatrixVariableException - ex: {}.", ExceptionUtils.getStackTrace(ex));

        String errorCode = ErrorCode.MULTIPART_EXPECTED.name();
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        List<Argument> arguments = new ArrayList<>();
        if (ex instanceof MaxUploadSizeExceededException maxUploadSizeExceededException) {
            long maxSize = maxUploadSizeExceededException.getMaxUploadSize();
            errorCode = ErrorCode.MAX_SIZE.name();
            arguments.add(new Argument("max_size", maxSize));
        }
        String formattedMessage = templateParser.interpolate(new ErrorMessage(errorCode, arguments, ex.getMessage()));

        return new ResponseEntity<>(new LocalizedErrorResponse(errorCode, formattedMessage), httpStatus);
    }

    @ResponseBody
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> handleResponseStatusException(Throwable ex) {
        log.error("{} occurred. handleMissingRequestParametersException - ex: {}.", ex.getClass().getSimpleName(), ExceptionUtils.getStackTrace(ex));

        List<Argument> args = null;
        ErrorCode errorCode = ErrorCode.UNKNOWN_ERROR;
        if (ex instanceof WebExchangeBindException webExchangeBindException) {
            return handleWebExchangeBindException(webExchangeBindException);
        } else {
            if (ex instanceof ServerWebInputException serverWebInputException) {
                MethodParameter parameter = serverWebInputException.getMethodParameter();

                if (ex.getCause() instanceof TypeMismatchException cause) {
                    if (parameter != null) {
                        cause.initPropertyName(Objects.requireNonNull(parameter.getParameterName()));
                    }
                    return handleTypeMismatchException(cause);
                }

                LocalizedExceptionWithStatus result = handleMissingParameters(parameter);
                if (result != null) {
                    return new ResponseEntity<>(result.localizedErrorResponse, result.status);
                }

                errorCode = ErrorCode.INVALID_OR_MISSING_BODY;
            }

            if (ex instanceof UnsupportedMediaTypeStatusException unsupportedMediaTypeStatusException) {
                Set<String> types = getMediaTypes(unsupportedMediaTypeStatusException.getSupportedMediaTypes());
                args = types.isEmpty() ? Collections.emptyList() : Collections.singletonList(new Argument(TYPES, types));
                errorCode = ErrorCode.NOT_SUPPORTED;
            }

            if (ex instanceof NotAcceptableStatusException notAcceptableStatusException) {
                Set<String> types = getMediaTypes(notAcceptableStatusException.getSupportedMediaTypes());
                args = types.isEmpty() ? Collections.emptyList() : Collections.singletonList(new Argument(TYPES, types));
                errorCode = ErrorCode.NOT_ACCEPTABLE;
            }

            if (ex instanceof MethodNotAllowedException methodNotAllowedException) {
                String httpMethod = methodNotAllowedException.getHttpMethod();
                args = Collections.singletonList(new Argument("method", httpMethod));
                errorCode = ErrorCode.METHOD_NOT_ALLOWED;
            }

            if (ex instanceof ResponseStatusException responseStatusException) {
                HttpStatusCode status = responseStatusException.getStatusCode();
                if (status == HttpStatus.NOT_FOUND) {
                    errorCode = ErrorCode.NO_HANDLER;
                }
            }
        }
        String formattedMessage = templateParser.interpolate(new ErrorMessage(errorCode.name(), args, ex.getMessage()));

        return new ResponseEntity<>(new LocalizedErrorResponse(errorCode.name(), formattedMessage), errorCode.getHttpStatus());
    }

    @ResponseBody
    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<?> handleWebExchangeBindException(WebExchangeBindException ex) {
        log.error("{} occurred. handleWebExchangeBindException - ex: {}.", ex.getClass().getSimpleName(), ExceptionUtils.getStackTrace(ex));

        String errorCode = ErrorCode.VALIDATION_ERROR.name();
        HttpStatus httpStatus = ErrorCode.VALIDATION_ERROR.getHttpStatus();
        BindingResult bindingResult = ex.getBindingResult();
        List<ValidationErrorDetail.ValidationError> errors = bindingResult.getFieldErrors().stream().map(error -> {
            ErrorMessage errorMessage = messageBuilder.errorMessage(error, error.getDefaultMessage());
            templateParser.interpolate(errorMessage);
            return new ValidationErrorDetail.ValidationError(error.getField(), error.getRejectedValue(), error.getDefaultMessage(), errorMessage.getErrorCode());
        }).collect(Collectors.toList());
        ErrorMessage errorMessage = new ErrorMessage(errorCode, List.of(new Argument("size", errors.size())), String.format("Validation failed for object='%s'. Error count: %s", bindingResult.getObjectName(), errors.size()));
        String formattedMessage = templateParser.interpolate(errorMessage);

        return new ResponseEntity<>(new LocalizedErrorResponse(errorCode, formattedMessage, new ValidationErrorDetail(errors)), httpStatus);
    }

    @ResponseBody
    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<?> handleTypeMismatchException(TypeMismatchException ex) {
        log.error("TypeMismatchException occurred. handleTypeMismatchException - ex: {}.", ExceptionUtils.getStackTrace(ex));

        List<Argument> arguments = messageBuilder.getArguments(ex);
        String errorCode = messageBuilder.getErrorCode(ex);
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        String formattedMessage = templateParser.interpolate(new ErrorMessage(errorCode, arguments, ex.getMessage()));

        return new ResponseEntity<>(new LocalizedErrorResponse(errorCode, formattedMessage), httpStatus);
    }

    @ResponseBody
    @ExceptionHandler({HttpMediaTypeNotAcceptableException.class,
            HttpMediaTypeNotSupportedException.class,
            HttpRequestMethodNotSupportedException.class,
            MissingServletRequestPartException.class,
            NoHandlerFoundException.class})
    public ResponseEntity<?> handleServletWebException(Throwable ex) {
        log.error("{} occurred. handleServletWebException - ex: {}.", ex.getClass().getSimpleName(), ExceptionUtils.getStackTrace(ex));

        ErrorCode errorCode = ErrorCode.UNKNOWN_ERROR;
        List<Argument> args = null;

        if (ex instanceof HttpMediaTypeNotAcceptableException httpMediaTypeNotAcceptableException) {
            Set<String> types = this.getMediaTypes(httpMediaTypeNotAcceptableException.getSupportedMediaTypes());
            errorCode = ErrorCode.NOT_ACCEPTABLE;
            args = types.isEmpty() ? Collections.emptyList() : Collections.singletonList(new Argument(TYPES, types));
        }

        if (ex instanceof HttpMediaTypeNotSupportedException httpMediaTypeNotSupportedException) {
            MediaType contentType = httpMediaTypeNotSupportedException.getContentType();
            if (contentType != null) {
                args = Collections.singletonList(new Argument("type", contentType.toString()));
            }

            errorCode = ErrorCode.NOT_SUPPORTED;
        }

        String url;
        if (ex instanceof HttpRequestMethodNotSupportedException httpRequestMethodNotSupportedException) {
            url = httpRequestMethodNotSupportedException.getMethod();
            errorCode = ErrorCode.METHOD_NOT_ALLOWED;
            args = Collections.singletonList(new Argument("method", url));
        }

        if (ex instanceof MissingServletRequestPartException missingServletRequestPartException) {
            url = missingServletRequestPartException.getRequestPartName();
            errorCode = ErrorCode.MISSING_PART;
            args = Collections.singletonList(new Argument("name", url));
        }

        if (ex instanceof NoHandlerFoundException noHandlerFoundException) {
            url = noHandlerFoundException.getRequestURL();
            errorCode = ErrorCode.NO_HANDLER;
            args = Collections.singletonList(new Argument("path", url));
        }
        String formattedMessage = templateParser.interpolate(new ErrorMessage(errorCode.name(), args, ex.getMessage()));

        return new ResponseEntity<>(new LocalizedErrorResponse(errorCode.name(), formattedMessage), errorCode.getHttpStatus());
    }

    private LocalizedExceptionWithStatus handleMissingParameters(MethodParameter parameter) {
        if (parameter == null) {
            return null;
        }

        ErrorCode code = ErrorCode.UNKNOWN_ERROR;
        String parameterName = null;

        RequestHeader requestHeader = parameter.getParameterAnnotation(RequestHeader.class);
        if (requestHeader != null) {
            code = ErrorCode.MISSING_HEADER;
            parameterName = this.extractParameterName(requestHeader, parameter);
        }

        RequestParam requestParam = parameter.getParameterAnnotation(RequestParam.class);
        if (requestParam != null) {
            code = ErrorCode.MISSING_PARAMETER;
            parameterName = this.extractParameterName(requestParam, parameter);
        }

        RequestPart requestPart = parameter.getParameterAnnotation(RequestPart.class);
        if (requestPart != null) {
            code = ErrorCode.MISSING_PART;
            parameterName = this.extractParameterName(requestPart, parameter);
        }

        CookieValue cookieValue = parameter.getParameterAnnotation(CookieValue.class);
        if (cookieValue != null) {
            code = ErrorCode.MISSING_COOKIE;
            parameterName = this.extractParameterName(cookieValue, parameter);
        }

        MatrixVariable matrixVariable = parameter.getParameterAnnotation(MatrixVariable.class);
        if (matrixVariable != null) {
            code = ErrorCode.MISSING_MATRIX_VARIABLE;
            parameterName = this.extractParameterName(matrixVariable, parameter);
        }

        String errorCode = code.name();
        HttpStatus httpStatus = code.getHttpStatus();
        ErrorMessage errorMessage = new ErrorMessage(errorCode, Arrays.asList(new Argument("name", parameterName), new Argument(EXPECTED, parameter.getParameterType().getName())), null);
        String formattedMessage = templateParser.interpolate(errorMessage);

        return new LocalizedExceptionWithStatus(httpStatus, new LocalizedErrorResponse(errorCode, formattedMessage));
    }

    private String extractParameterName(Annotation annotation, MethodParameter parameter) {
        String name = this.getNameAttribute(annotation);
        return name.isEmpty() ? parameter.getParameterName() : name;
    }

    private String getNameAttribute(Annotation annotation) {
        try {
            Method method = annotation.getClass().getMethod("name");
            return (String) method.invoke(annotation);
        } catch (Exception ex) {
            return "";
        }
    }

    private Set<String> getMediaTypes(List<MediaType> mediaTypes) {
        return mediaTypes == null ? Collections.emptySet() : mediaTypes.stream().map(MimeType::toString).collect(Collectors.toSet());
    }

    @RequiredArgsConstructor
    private static class LocalizedExceptionWithStatus {
        private final HttpStatus status;
        private final LocalizedErrorResponse localizedErrorResponse;
    }
}
