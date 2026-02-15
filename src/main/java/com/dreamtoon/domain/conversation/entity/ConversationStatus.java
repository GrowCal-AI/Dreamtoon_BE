package com.dreamtoon.domain.conversation.entity;

/** 대화 세션 상태 */
public enum ConversationStatus {
    /** 진행 중 - 사용자와 AI가 대화를 주고받는 중 */
    IN_PROGRESS,

    /** 완료 - 대화가 정상적으로 종료되고 꿈이 생성됨 */
    COMPLETED,

    /** 중단됨 - 사용자가 대화를 완료하지 않고 종료함 */
    ABANDONED
}
