package com.elice.homealone.module.member.service;

import com.elice.homealone.global.exception.ErrorCode;
import com.elice.homealone.global.exception.HomealoneException;
import com.elice.homealone.global.jwt.JwtTokenProvider;
import com.elice.homealone.global.redis.RedisUtil;
import com.elice.homealone.module.member.dto.MemberDto;
import com.elice.homealone.module.member.dto.request.LoginRequestDto;
import com.elice.homealone.module.member.dto.request.SignupRequestDto;
import com.elice.homealone.module.member.dto.TokenDto;
import com.elice.homealone.module.member.entity.Member;
import com.elice.homealone.module.member.repository.MemberRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService{

    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RedisUtil redisUtil;
    private final String GRANT_TYPE = "Bearer ";
    @Value("${spring.jwt.token.refresh-expiration-time}")
    private int refreshExpirationTime;

    /**
     * 회원 가입
     */
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

    /**
     * 로그인
     */
    public TokenDto login(LoginRequestDto loginRequestDTO, HttpServletResponse httpServletResponse) {
        Member findMember = memberService.findByEmail(loginRequestDTO.getEmail());
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

    /**
     * 회원 deltedAt 유무 검증
     */
    public void isAccountDeleted(Member member) {
        if(!member.isEnabled()) throw new HomealoneException(ErrorCode.MEMBER_NOT_FOUND);
    }

    /**
     * 로그아웃
     */

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

    /**
     * refreshToken 쿠키에 저장
     */
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

    /**
     * 이메일 중복여부 검사
     */
    public boolean isEmailDuplicate(String email) {
        if(memberRepository.findByEmail(email).isPresent()){
            throw new HomealoneException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        return false;
    }

    /**
     * 회원 수정
     * Auth: User
     */
    public Member editMember(MemberDto memberDTO) {
        Member member = getMember();
        Optional.ofNullable(memberDTO.getName()).ifPresent(name->member.setName(name));
        Optional.ofNullable(memberDTO.getBirth()).ifPresent(birth->member.setBirth(birth));
        Optional.ofNullable(memberDTO.getFirstAddress()).ifPresent(first->member.setFirstAddress(first));
        Optional.ofNullable(memberDTO.getSecondAddress()).ifPresent(second->member.setSecondAddress(second));
        Optional.ofNullable(memberDTO.getImageUrl()).ifPresent(address->member.setImageUrl(address));
        memberRepository.save(member);
        return member;
    }

    /**
     * 회원 삭제 delete
     */
    public void deleteMember(Long memberId) {
        Member findedMember = memberService.findById(memberId);
        memberRepository.delete(findedMember);
    }

    /**
     * 회원 탈퇴 withdrawal
     */
    public boolean withdrawal(Member member) {
        Member findedMember = memberService.findByEmail(member.getEmail());
        findedMember.setDeletedAt(true);
        return true;
    }

    /**
     * 회원 정보 받아오는 메소드
     */
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

