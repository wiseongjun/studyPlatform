package com.example.chapter.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import com.example.chapter.dto.internal.ChapterListCondition;
import com.example.chapter.dto.res.ChapterListResponse;
import com.example.chapter.entity.QChapter;
import com.example.chapter.repository.support.ChapterQuerySupport;

@Repository
@RequiredArgsConstructor
public class ChapterRepository {

	private final JPAQueryFactory queryFactory;

	public List<ChapterListResponse> getChapterList(ChapterListCondition condition) {
		QChapter chapter = QChapter.chapter;

		return queryFactory
			.select(Projections.constructor(ChapterListResponse.class,
				chapter.id,
				chapter.name,
				chapter.category
			))
			.from(chapter)
			.where(
				chapter.deleted.isFalse(),
				ChapterQuerySupport.nameContains(condition.getName()),
				ChapterQuerySupport.categoryEq(condition.getCategory())
			)
			.fetch();
	}
}
