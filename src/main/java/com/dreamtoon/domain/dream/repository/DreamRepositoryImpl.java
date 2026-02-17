package com.dreamtoon.domain.dream.repository;

import com.dreamtoon.domain.dream.entity.Dream;
import com.dreamtoon.domain.dream.entity.Genre;
import com.dreamtoon.domain.dream.entity.QDream;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class DreamRepositoryImpl implements DreamRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Dream> findLibraryDreams(
            Long userId,
            Boolean favorite,
            Genre genre,
            String search,
            String sort,
            Pageable pageable) {
        QDream dream = QDream.dream;

        JPAQuery<Dream> query =
                queryFactory
                        .selectFrom(dream)
                        .where(
                                dream.user.id.eq(userId),
                                dream.isInLibrary.eq(true),
                                favoriteOnly(favorite, dream),
                                genreEq(genre, dream),
                                searchInTitleOrContent(search, dream))
                        .orderBy(dream.createdAt.desc());

        var content = query.offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch();

        JPAQuery<Long> countQuery =
                queryFactory
                        .select(dream.count())
                        .from(dream)
                        .where(
                                dream.user.id.eq(userId),
                                dream.isInLibrary.eq(true),
                                favoriteOnly(favorite, dream),
                                genreEq(genre, dream),
                                searchInTitleOrContent(search, dream));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression favoriteOnly(Boolean favorite, QDream dream) {
        return favorite == null ? null : dream.isFavorite.eq(favorite);
    }

    private BooleanExpression genreEq(Genre genre, QDream dream) {
        return genre == null ? null : dream.selectedGenre.eq(genre);
    }

    private BooleanExpression searchInTitleOrContent(String search, QDream dream) {
        if (search == null || search.isBlank()) {
            return null;
        }
        String pattern = "%" + search.trim().toLowerCase() + "%";
        return dream.title.lower().like(pattern).or(dream.dreamContent.lower().like(pattern));
    }
}
