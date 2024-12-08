package com.elice.homealone.module.login.service;

import com.elice.homealone.global.exception.ErrorCode;
import com.elice.homealone.global.exception.HomealoneException;
import com.elice.homealone.global.jwt.JwtTokenProvider;
import com.elice.homealone.global.redis.RedisUtil;
import com.elice.homealone.module.login.dto.request.LoginRequestDto;
import com.elice.homealone.module.login.dto.request.SignupRequestDto;
import com.elice.homealone.module.login.dto.TokenDto;
import com.elice.homealone.module.member.entity.Member;
import com.elice.homealone.module.member.repository.MemberRepository;
import com.elice.homealone.module.member.service.MemberQueryService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final MemberQueryService memberQueryService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RedisUtil redisUtil;
    private final String GRANT_TYPE = "Bearer ";
    @Value("${spring.jwt.token.refresh-expiration-time}")
    private int refreshExpirationTime;

    public void signUp(SignupRequestDto signupRequestDTO){
        //이메일 중복검사
        isEmailDuplicate(signupRequestDTO.getEmail());
        //비밀번호 암호화
        String password = passwordEncoder.encode(signupRequestDTO.getPassword());
        Member savedMember = Member.from(signupRequestDTO);
        savedMember.setPassword(password);
        //회원 저장
        memberRepository.save(savedMember);
    }

    public TokenDto login(LoginRequestDto loginRequestDTO, HttpServletResponse httpServletResponse) {
        Member findMember = memberQueryService.findByEmail(loginRequestDTO.getEmail());
        isAccountDeleted(findMember);
        if (passwordEncoder.matches(loginRequestDTO.getPassword(), findMember.getPassword())) {
            String acessToken = GRANT_TYPE + jwtTokenProvider.createAccessToken(findMember.getEmail());
            String refreshToken = jwtTokenProvider.createRefreshToken(findMember.getEmail()); //쿠키는 공백이 저장되지 않음
            TokenDto response = new TokenDto();
            response.setAccessToken(acessToken);
            //refreshToken 쿠키 저장
            httpServletResponse.addCookie(storeRefreshToken(refreshToken));
            return response;
        } else{
            throw new HomealoneException(ErrorCode.MISMATCHED_PASSWORD);
        }
    }

    public void isAccountDeleted(Member member) {
        if(!member.isEnabled()) throw new HomealoneException(ErrorCode.MEMBER_NOT_FOUND);
    }

    public void logout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse){
        String acccessToken = httpServletRequest.getHeader("Authorization");
        //1. accessToken을 블랙리스트 redis에 저장
        redisUtil.setBlackList(acccessToken.substring(7),"blacklist");
        //2. refreshToken을 쿠키에서 삭제
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        httpServletResponse.addCookie(cookie);
    }

    public Cookie storeRefreshToken(String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(false);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(refreshExpirationTime);
        return cookie;
    }

    public TokenDto refreshAccessToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String refreshToken = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                }
            }
        }

        // 1. Refresh Token 검증
        jwtTokenProvider.validateRefreshToken(refreshToken);
        // 2. Refresh Token에서 사용자 정보 추출
        String email = jwtTokenProvider.getEmail(refreshToken);
        // 3. 새로운 Access Token 생성
        String newAccessToken = jwtTokenProvider.createAccessToken(email);
        TokenDto tokenDto = new TokenDto();
        tokenDto.setAccessToken(GRANT_TYPE+newAccessToken);

        return tokenDto;
    }

    public boolean isEmailDuplicate(String email) {
        if(memberRepository.findByEmail(email).isPresent()){
            throw new HomealoneException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        return false;
    }

    public Member getMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //비회원 처리
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        //멤버 객체 예외 처리
        if (principal instanceof Member) {
            return (Member) principal;
        } else {
            throw new HomealoneException(ErrorCode.MEMBER_NOT_FOUND);
        }
    }


    /**
     * 스프링 시큐리티 인증 로직
     * email을 통해서 SecurityContextHolder에 사용자를 저장해둔다.
     */
    @Override
    public Member loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberQueryService.findByEmail(email);
        return member;
    }

    /**
     * 관리자 권한인지 아닌지 확인하는 메소드
     * @param member
     * @return
     */
    public Boolean isAdmin(Member member) {
        return member.getAuthorities()
                .stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }
}

