package com.elice.homealone.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


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
    @Setter
    public static class URLResponse{

        private String imageUrl;
        private String fileName;

        public URLResponse(String url, String fileName) {
            this.imageUrl = url;
            this.fileName = fileName;

        }
    }

    @Getter
    @AllArgsConstructor
    public static class ErrorResponse {
        private final int status;
        private final String error;
        private final String message;
    }
}
