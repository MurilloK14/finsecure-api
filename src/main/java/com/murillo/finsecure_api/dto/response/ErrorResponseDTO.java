package com.murillo.finsecure_api.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponseDTO(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
        ) {

    public ErrorResponseDTO(int status, String error, String message, String path, Map<String, String> fieldErrors) {
        this(LocalDateTime.now(),status,error,message,path,null);
    }

}
