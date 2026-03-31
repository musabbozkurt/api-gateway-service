package com.mb.notificationservice.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private T data;
    private String errorCode;
    private String message;
    private boolean success;

    public ApiResponse(T data) {
        this.data = data;
        this.success = true;
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(data, null, null, true);
    }

    public static <T> ApiResponse<T> error(String errorCode, String message) {
        return new ApiResponse<>(null, errorCode, message, false);
    }
}
