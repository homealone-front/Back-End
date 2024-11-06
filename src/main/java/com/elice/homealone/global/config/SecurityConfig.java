package com.elice.homealone.global.config;

import com.elice.homealone.global.jwt.JwtAuthenticationFilter;
import com.elice.homealone.global.jwt.JwtTokenProvider;
import com.elice.homealone.global.redis.RedisUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final RedisUtil redisUtil;
    private final WebConfig webConfig;
    
    private final String[] admin = {
            "/api/admin/**"
    };
    //임시로 모든 회원정보 모두 허용
    private final String[] member = {
            "/**"
    };
    private final String[] resource = {
            "/swagger-ui/**", "/swagger-ui.html"
    };

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService, redisUtil);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(webConfig.corsConfigurationSource())) // CORS 설정 적용
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)) //H2
                .formLogin(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        auth -> auth.requestMatchers(admin).hasRole("ADMIN")
                                    .requestMatchers(member).permitAll()
                                    .requestMatchers(resource).permitAll()
                                    .requestMatchers("/static/index.html", "/api/**", "/**").permitAll()
                                    .requestMatchers("/api/usedtrade").permitAll()
                                    .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                                    .anyRequest().permitAll() //임시설정
                )
                .logout(logout -> logout.logoutUrl("/logout")
                                        .invalidateHttpSession(true)
                                        .deleteCookies("JSESSIONID")
                                        .logoutSuccessHandler((request, response, authentication) -> {
                                            response.setStatus(HttpServletResponse.SC_OK);
                                            response.getWriter().flush();
                                        })
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService,redisUtil), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

}
