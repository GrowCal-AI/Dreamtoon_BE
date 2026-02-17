package com.dreamtoon.domain.dream.entity;

/** Dream AI 처리 상태 */
public enum ProcessingStatus {
    /** 대기 중 - Dream이 생성되었지만 AI 처리가 시작되지 않음 */
    PENDING,

    /** 분석 중 - GPT-4o 꿈 분석이 진행 중 */
    ANALYZING,

    /** 분석 완료 - 꿈 분석 완료, 장르 선택 대기 중 */
    ANALYSIS_COMPLETED,

    /** 생성 중 - DALL-E 4컷 만화 이미지 생성 진행 중 */
    GENERATING,

    /** 완료 - 모든 처리가 성공적으로 완료됨 */
    COMPLETED,

    /** 실패 - AI 처리 중 오류 발생 */
    FAILED
}
