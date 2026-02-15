package com.dreamtoon.domain.conversation.entity;

/** 메시지 발신자 역할 */
public enum MessageRole {
    /** 사용자 메시지 */
    USER,

    /** AI 어시스턴트 메시지 */
    ASSISTANT,

    /** 시스템 메시지 (자동 생성된 안내 메시지) */
    SYSTEM
}
