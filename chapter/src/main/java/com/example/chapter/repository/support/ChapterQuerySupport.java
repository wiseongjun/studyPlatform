package com.example.chapter.repository.support;

import com.querydsl.core.types.dsl.BooleanExpression;

import com.example.chapter.entity.QChapter;
import com.example.constants.ChapterCategory;

public final class ChapterQuerySupport {

	private ChapterQuerySupport() {
	}

	public static BooleanExpression nameContains(String name) {
		return name != null ? QChapter.chapter.name.containsIgnoreCase(name) : null;
	}

	public static BooleanExpression categoryEq(ChapterCategory category) {
		return category != null ? QChapter.chapter.category.eq(category) : null;
	}
}
