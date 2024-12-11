package com.elice.homealone.global.jwt;

import com.elice.homealone.global.exception.AuthErrorResponseHandler;
import com.elice.homealone.global.exception.ErrorCode;
import com.elice.homealone.global.exception.RefreshTokenException;
import com.elice.homealone.global.exception.TokenException;
import com.elice.homealone.global.redis.RedisUtil;
import com.elice.homealone.module.member.entity.Member;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;


public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final RedisUtil redisUtil;
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final AuthErrorResponseHandler authErrorResponseHandler;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService, RedisUtil redisUtil
            , HandlerExceptionResolver handlerExceptionResolver,  AuthErrorResponseHandler authErrorResponseHandler) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
        this.redisUtil = redisUtil;
        this.handlerExceptionResolver = handlerExceptionResolver;
        this.authErrorResponseHandler = authErrorResponseHandler;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException{
        //HTTP 요청에서 JWT 토큰을 추출
        String token = jwtTokenProvider.resolveToken(request);

        //토큰값이 null이 아니면서 Redis에 블랙리스트로 등록된 토큰인 경우
        if (token != null && redisUtil.hasKeyBlackList(token)) {
            throw new TokenException(ErrorCode.INVALID_TOKEN);
        }
        try{
            //토큰값이 null이 아니면서 유효한 access token인 경우
            if (token != null && jwtTokenProvider.validateAccessToken(token)) {
                String email = jwtTokenProvider.getEmail(token);
                Member member = (Member) userDetailsService.loadUserByUsername(email);
                if (member != null) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(member, null, member.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }catch (TokenException e){
            //access token이 만료되어 refresh 요청을 보내는 경우에 필터체인 패스
            if(e.getErrorCode().getCode().equals(ErrorCode.EXPIRED_ACCESS_TOKEN.getCode())
                    && request.getRequestURI().equals("/api/token/refresh")) {
                filterChain.doFilter(request, response);
                return;
            }
            authErrorResponseHandler.writeAuthErrorResponse(response, e);
        }
        filterChain.doFilter(request, response);
    }
}

