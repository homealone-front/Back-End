package com.elice.homealone.global.exception.response;

import com.elice.homealone.global.exception.TokenException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;


public class Response {
    @Getter
    @Setter
    public static class ApiResponse{
        private String message;
        public ApiResponse(String message) {
            this.message = message;
        }
    }

    @Getter
    @AllArgsConstructor
    public static class ErrorResponse {
        private final int status;
        private final String code;
        private final String message;
    }


}
