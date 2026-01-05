package com.fooddelivery.authservice.exception;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ApiResponse<T> {
    private int status;
    private String message;
    private T data;
    private LocalDateTime timeStamp;
}
