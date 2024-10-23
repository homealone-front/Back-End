package com.elice.homealone.global.jwt;

import com.elice.homealone.global.exception.ErrorCode;
import com.elice.homealone.global.exception.HomealoneException;
import com.elice.homealone.global.redis.RedisUtil;
import com.elice.homealone.module.member.entity.Member;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final RedisUtil redisUtil;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService, RedisUtil redisUtil) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
        this.redisUtil = redisUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String token = jwtTokenProvider.resolveToken(request);
            // 로그아웃이 된 토큰으로 접근 시도할 때
            if (token != null && redisUtil.hasKeyBlackList(token)) {
                // TODO 에러처리 추가
                throw new HomealoneException(ErrorCode.INVALID_TOKEN);
            }
            if (token != null && jwtTokenProvider.validateToken(token)) {
                String email = jwtTokenProvider.getEmail(token);
                Member member = (Member) userDetailsService.loadUserByUsername(email);
                if (member != null) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(member, null, member.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            handleTokenException(request, response, e, "/api/token/refresh", filterChain);
        } catch (JwtException e) {
            setErrorResponse(request, response, e);
        }
    }

    private void setErrorResponse(HttpServletRequest req, HttpServletResponse res, Throwable ex) throws IOException {
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        final Map<String, Object> body = new HashMap<>();
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("error", "UNAUTHORIZED");
        body.put("message", ex.getMessage());
        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(res.getOutputStream(), body);
        res.setStatus(HttpServletResponse.SC_OK);
    }

    private void handleTokenException(HttpServletRequest request, HttpServletResponse response, Exception e, String refreshTokenPath, FilterChain filterChain) throws IOException, ServletException {
        String path = request.getRequestURI();
        if (path.equals(refreshTokenPath)) {
            filterChain.doFilter(request, response);
        } else {
            setErrorResponse(request, response, e);
        }
    }

}

