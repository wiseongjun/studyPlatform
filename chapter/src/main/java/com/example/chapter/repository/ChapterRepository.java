package com.example.chapter.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import com.example.chapter.dto.ChapterResponse;

@Repository
@RequiredArgsConstructor
public class ChapterRepository {

	private final JPAQueryFactory queryFactory;

	public List<ChapterResponse> getChapterList() {
		return null;
	}
}

