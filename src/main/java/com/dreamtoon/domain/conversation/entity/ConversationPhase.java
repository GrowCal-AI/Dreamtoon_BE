package com.dreamtoon.domain.conversation.entity;

/** 대화 단계 - AI 에이전트가 수행하는 단계별 작업 */
public enum ConversationPhase {
        /** 감정 수집 단계 - 6대 감정 칩 선택 및 강도 측정 */
        EMOTION_COLLECTION,

        /** 상황 분석 단계 - 스트레스 평가 및 현실 상황 분석 */
        CONTEXT_ANALYSIS,

        /** 리포트 생성 단계 - 심리 리포트 및 AI 인사이트 생성 */
        REPORT_GENERATION,

        /** 콘텐츠 생성 단계 - 웹툰 스타일 선택 및 생성 */
        CONTENT_CREATION
}
