package com.dreamtoon.global.common.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * API 공통 응답 DTO
 *
 * <p>모든 API 엔드포인트의 표준 응답 형식을 정의합니다.
 *
 * @param <T> 응답 데이터의 타입
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /** 요청 성공 여부 */
    private boolean success;

    /** 응답 메시지 */
    private String message;

    /** 응답 데이터 (nullable) */
    private T data;

    // ========== 정적 팩토리 메서드: 성공 응답 ==========

    /**
     * 성공 응답 생성 (데이터 포함)
     *
     * @param data 응답 데이터
     * @param <T> 데이터 타입
     * @return 성공 응답 인스턴스
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("요청이 성공적으로 처리되었습니다.")
                .data(data)
                .build();
    }

    /**
     * 성공 응답 생성 (커스텀 메시지 + 데이터)
     *
     * @param message 커스텀 메시지
     * @param data 응답 데이터
     * @param <T> 데이터 타입
     * @return 성공 응답 인스턴스
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder().success(true).message(message).data(data).build();
    }

    /**
     * 성공 응답 생성 (데이터 없음)
     *
     * @return 성공 응답 인스턴스 (data = null)
     */
    public static ApiResponse<Void> success() {
        return ApiResponse.<Void>builder().success(true).message("요청이 성공적으로 처리되었습니다.").build();
    }

    /**
     * 성공 응답 생성 (커스텀 메시지, 데이터 없음)
     *
     * @param message 커스텀 메시지
     * @return 성공 응답 인스턴스 (data = null)
     */
    public static ApiResponse<Void> success(String message) {
        return ApiResponse.<Void>builder().success(true).message(message).build();
    }

    // ========== 정적 팩토리 메서드: 실패 응답 ==========

    /**
     * 실패 응답 생성 (에러 메시지만)
     *
     * @param message 에러 메시지
     * @param <T> 데이터 타입
     * @return 실패 응답 인스턴스
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder().success(false).message(message).build();
    }

    /**
     * 실패 응답 생성 (에러 메시지 + 상세 데이터)
     *
     * @param message 에러 메시지
     * @param data 에러 상세 정보 (예: validation errors)
     * @param <T> 데이터 타입
     * @return 실패 응답 인스턴스
     */
    public static <T> ApiResponse<T> error(String message, T data) {
        return ApiResponse.<T>builder().success(false).message(message).data(data).build();
    }
}
