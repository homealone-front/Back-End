package com.elice.homealone.global.exception;

import com.elice.homealone.global.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         org.springframework.security.core.AuthenticationException authException) throws IOException {
        String path = request.getRequestURI();
        if (path.equals("/api/token/refresh")) return;
        Throwable cause = authException.getCause();
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        if (cause instanceof HomealoneException homealoneException) {
            final Map<String, Object> body = new HashMap<>();
            body.put("error", "UNAUTHORIZED");
            body.put("message", homealoneException.getMessage());
            body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
            final ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(response.getOutputStream(), body);
        } else {
            response.getWriter().write("{\"error\": \"알 수 없는 인증 오류\"}");
        }
    }
}