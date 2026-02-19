package com.dreamtoon.domain.dream.dto;

import lombok.Builder;
import lombok.Getter;

/** 비회원 꿈 생성 응답. guestToken으로 이후 조회/생성 API에 접근. */
@Getter
@Builder
public class GuestDreamCreateResponse {

    private Long dreamId;

    /** 이후 폴링/조회 시 ?token=xxx 로 전달해야 하는 1회성 접근 토큰 */
    private String guestToken;

    private String message;
}
