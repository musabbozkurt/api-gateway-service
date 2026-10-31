package com.mb.modulith.email.exception.handle;

import com.mb.modulith.email.exception.EmailErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {
    private EmailErrorCode emailErrorCode;
    private String message;
    private HttpStatus httpStatus;
}
