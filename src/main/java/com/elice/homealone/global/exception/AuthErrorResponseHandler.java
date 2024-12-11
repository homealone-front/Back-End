package com.elice.homealone.global.exception;

import com.elice.homealone.global.exception.response.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
public class AuthErrorResponseHandler {
    private final ObjectMapper objectMapper;

    public AuthErrorResponseHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void writeAuthErrorResponse(HttpServletResponse response, TokenException e) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        // ErrorResponse를 생성하여 응답
        Response.ErrorResponse errorResponse = new Response.ErrorResponse(
                HttpServletResponse.SC_UNAUTHORIZED,
                e.getErrorCode().getCode(),
                e.getMessage()
        );
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}