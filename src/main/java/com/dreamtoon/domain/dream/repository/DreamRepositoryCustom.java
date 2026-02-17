package com.dreamtoon.domain.dream.repository;

import com.dreamtoon.domain.dream.entity.Dream;
import com.dreamtoon.domain.dream.entity.Genre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** 라이브러리 필터링용 커스텀 리포지토리 */
public interface DreamRepositoryCustom {

    /**
     * 라이브러리 꿈 목록 조회 (isInLibrary=true, optional: favorite, genre, search, sort)
     *
     * @param userId 사용자 ID
     * @param favorite 즐겨찾기만 (null이면 전체)
     * @param genre 장르 (null이면 전체)
     * @param search 제목/내용 검색 (null/blank면 미적용)
     * @param sort 정렬 (latest 기본)
     * @param pageable 페이징
     */
    Page<Dream> findLibraryDreams(
            Long userId,
            Boolean favorite,
            Genre genre,
            String search,
            String sort,
            Pageable pageable);
}
