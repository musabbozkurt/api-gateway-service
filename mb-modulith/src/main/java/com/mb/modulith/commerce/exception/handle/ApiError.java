package com.mb.modulith.commerce.exception.handle;

import com.mb.modulith.commerce.exception.SharedErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {
    private SharedErrorCode sharedErrorCode;
    private String message;
    private int httpStatus;
}
