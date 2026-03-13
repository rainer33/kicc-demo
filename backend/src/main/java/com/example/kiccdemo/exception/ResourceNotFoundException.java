package com.example.kiccdemo.exception;

/**
 * 조회 대상 리소스가 없을 때 사용하는 예외입니다.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
