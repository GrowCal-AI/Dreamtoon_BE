package com.dreamtoon.domain.dream.entity;

/**
 * Dream AI 처리 상태
 */
public enum ProcessingStatus {
    /**
     * 대기 중 - Dream이 생성되었지만 AI 처리가 시작되지 않음
     */
    PENDING,

    /**
     * 처리 중 - AI 분석 및 이미지 생성이 진행 중
     */
    PROCESSING,

    /**
     * 완료 - 모든 처리가 성공적으로 완료됨
     */
    COMPLETED,

    /**
     * 실패 - AI 처리 중 오류 발생 (fallback 데이터 사용)
     */
    FAILED
}
