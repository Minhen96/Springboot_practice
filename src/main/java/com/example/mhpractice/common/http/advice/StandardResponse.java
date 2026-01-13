package com.example.mhpractice.common.http.advice;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import com.example.mhpractice.common.exception.ErrorResponse;

@EqualsAndHashCode
@Data
@NoArgsConstructor
public class StandardResponse<T> {
    private boolean success;
    private T data;
    private ErrorResponse error;

    public static <T> StandardResponse<T> success() {
        StandardResponse<T> response = new StandardResponse<>();
        response.setSuccess(true);
        response.setData(null);
        return response;
    }

    public static <T> StandardResponse<T> success(T data) {
        StandardResponse<T> response = new StandardResponse<>();
        response.setSuccess(true);
        response.setData(data);
        return response;
    }

    public static <T> StandardResponse<T> error(ErrorResponse error) {
        StandardResponse<T> response = new StandardResponse<>();
        response.setSuccess(false);
        response.setError(error);
        return response;
    }
}
