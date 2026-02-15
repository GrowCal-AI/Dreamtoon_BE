package com.dreamtoon.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

        // Common
        INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다."),
        METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "지원하지 않는 HTTP 메서드입니다."),
        INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "서버 내부 오류가 발생했습니다."),
        INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C004", "잘못된 타입입니다."),
        HANDLE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "C005", "접근 권한이 없습니다."),

        // User
        USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
        DUPLICATE_EMAIL(HttpStatus.CONFLICT, "U002", "이미 사용 중인 이메일입니다."),
        UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "U003", "인증되지 않은 사용자입니다."),

        // Dream
        DREAM_NOT_FOUND(HttpStatus.NOT_FOUND, "D001", "꿈 기록을 찾을 수 없습니다."),
        DREAM_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "D002", "꿈 생성에 실패했습니다."),
        INVALID_STYLE_PRESET(HttpStatus.BAD_REQUEST, "D003", "유효하지 않은 스타일 프리셋입니다."),

        // AI
        AI_API_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "A001", "AI API 호출에 실패했습니다."),
        AI_RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "A002", "AI API 호출 한도를 초과했습니다."),

        // Storage
        FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S001", "파일 업로드에 실패했습니다."),
        FILE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S002", "파일 삭제에 실패했습니다."),
        INVALID_FILE_FORMAT(HttpStatus.BAD_REQUEST, "S003", "유효하지 않은 파일 형식입니다."),

        // Subscription
        GENERATION_LIMIT_EXCEEDED(
                        HttpStatus.FORBIDDEN, "SUB001", "무료 회원은 월 3회까지 생성 가능합니다. 프리미엄으로 업그레이드하세요."),
        SAVE_LIMIT_EXCEEDED(
                        HttpStatus.FORBIDDEN,
                        "SUB002",
                        "무료 회원은 최대 3개까지 저장 가능합니다. 기존 꿈을 삭제하거나 프리미엄으로 업그레이드하세요."),
        PREMIUM_STYLE_NOT_ALLOWED(HttpStatus.FORBIDDEN, "SUB003", "프리미엄 스타일은 유료 회원만 사용 가능합니다."),

        // Conversation
        CONVERSATION_NOT_FOUND(HttpStatus.NOT_FOUND, "CONV001", "대화를 찾을 수 없습니다."),
        CONVERSATION_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "CONV002", "이미 완료된 대화입니다."),
        INVALID_CONVERSATION_PHASE(HttpStatus.BAD_REQUEST, "CONV003", "현재 대화 단계에서는 수행할 수 없는 작업입니다.");

        private final HttpStatus status;
        private final String code;
        private final String message;
}
