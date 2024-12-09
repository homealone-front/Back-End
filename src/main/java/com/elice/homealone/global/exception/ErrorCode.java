package com.elice.homealone.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    //400 BAD REQUEST
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    RECIPE_CREATION_FAILED(HttpStatus.BAD_REQUEST, "레시피 생성에 실패했습니다."),
    CHATROOM_CREATION_FAILED(HttpStatus.BAD_REQUEST, "자신의 게시물에 메시지를 요청할 수 없습니다"),
    NOT_MY_CHATROOM(HttpStatus.BAD_REQUEST, "접근할 수 없는 채팅방입니다"),
    DUPLICATE_REQUEST(HttpStatus.BAD_REQUEST,"중복된 요청입니다"),

    //401
    MISMATCHED_PASSWORD(UNAUTHORIZED, "비밀번호가 일치하지 않습니다"),
    INVALID_TOKEN(UNAUTHORIZED, "INVALID_TOKEN"),
    EXPIRED_TOKEN(UNAUTHORIZED, "EXPIRED_ACCESS_TOKEN"),
    EXPIRED_REFRESH_TOKEN(UNAUTHORIZED, "EXPIRED_REFRESH_TOKEN"),
    MEMBER_NOT_AUTHENTICATED(UNAUTHORIZED, "인증되지 않은 회원입니다."),

    //403
    NOT_UNAUTHORIZED_ACTION(FORBIDDEN,"작성자와 일치하지 않습니다."),
    NO_JWT_TOKEN(FORBIDDEN,"JWT토큰이 비어있습니다."),

    //404
    POST_NOT_FOUND(NOT_FOUND, "존재하지 않는 게시물입니다."),
    EMAIL_NOT_FOUND(NOT_FOUND, "존재하지 않는 이메일입니다"),
    MEMBER_NOT_FOUND(NOT_FOUND,"존재하지 않는 회원입니다. "),
    ROOM_NOT_FOUND(NOT_FOUND, "존재하지 않는 게시물입니다"),
    TALK_NOT_FOUND(NOT_FOUND, "존재하지 않는 게시물입니다"),
    RECIPE_NOT_FOUND(NOT_FOUND, "존재하지 않는 레시피입니다"),
    CHATTING_ROOM_NOT_FOUND(NOT_FOUND, "존재하지 않는 채팅방입니다"),
    USEDTRADE_NOT_FOUND(NOT_FOUND,"존재하지 않는 게시물입니다"),
    SEARCH_NOT_FOUND(NOT_FOUND,"검색결과가 존재하지 않습니다"),
    WRITE_NOT_FOUND(NOT_FOUND,"작성하신 게시물이 존재하지 않습니다"),
    COMMENT_NOT_FOUND(NOT_FOUND, "조회되지 않는 댓글입니다."),


    //409
    EMAIL_ALREADY_EXISTS(CONFLICT, "중복된 이메일 입니다."),

    //500
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류입니다. 다시 요청해주세요."),
    STORAGE_NOT_FOUNT(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 파일을 저장할 저장소를 찾을 수 없습니다."),
    REDIS_NOT_INITIALIZED(HttpStatus.INTERNAL_SERVER_ERROR,"redisBlackListTemplate가 초기화 되지 않았습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
