package com.dreamtoon.global.common.dto.response;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

/**
 * 페이지네이션 응답 DTO
 *
 * <p>Spring Data의 Page 객체를 클라이언트 친화적인 형태로 변환합니다.
 *
 * @param <T> 페이지 내용의 타입
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PageResponse<T> {

        /** 현재 페이지의 데이터 목록 */
        private List<T> content;

        /** 현재 페이지 번호 (0부터 시작) */
        private int pageNumber;

        /** 페이지 크기 (한 페이지당 항목 수) */
        private int pageSize;

        /** 전체 항목 수 */
        private long totalElements;

        /** 전체 페이지 수 */
        private int totalPages;

        /** 첫 페이지 여부 */
        private boolean first;

        /** 마지막 페이지 여부 */
        private boolean last;

        /** 다음 페이지 존재 여부 */
        private boolean hasNext;

        /** 이전 페이지 존재 여부 */
        private boolean hasPrevious;

        /** 비어있는 페이지 여부 */
        private boolean empty;

        /**
         * Spring Data Page를 PageResponse로 변환 (정적 팩토리 메서드)
         *
         * @param page Spring Data Page 객체
         * @param <T> 페이지 내용의 타입
         * @return PageResponse 인스턴스
         */
        public static <T> PageResponse<T> of(Page<T> page) {
                return PageResponse.<T>builder()
                                .content(page.getContent())
                                .pageNumber(page.getNumber())
                                .pageSize(page.getSize())
                                .totalElements(page.getTotalElements())
                                .totalPages(page.getTotalPages())
                                .first(page.isFirst())
                                .last(page.isLast())
                                .hasNext(page.hasNext())
                                .hasPrevious(page.hasPrevious())
                                .empty(page.isEmpty())
                                .build();
        }

        /**
         * 빈 페이지 생성 (정적 팩토리 메서드)
         *
         * @param <T> 페이지 내용의 타입
         * @return 비어있는 PageResponse 인스턴스
         */
        public static <T> PageResponse<T> empty() {
                return PageResponse.<T>builder()
                                .content(List.of())
                                .pageNumber(0)
                                .pageSize(0)
                                .totalElements(0)
                                .totalPages(0)
                                .first(true)
                                .last(true)
                                .hasNext(false)
                                .hasPrevious(false)
                                .empty(true)
                                .build();
        }
}
