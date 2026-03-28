package com.example.type;

import com.example.exception.CustomException;
import com.example.exception.ErrorCode;

public enum ChapterCategory {

	JAVA("자바"),
	SPRING("스프링"),
	DATABASE("데이터베이스"),
	ALGORITHM("알고리즘"),
	DATA_STRUCTURE("자료구조");

	private final String label;

	ChapterCategory(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public static ChapterCategory from(String label) {
		for (ChapterCategory category : values()) {
			if (category.label.equals(label)) {
				return category;
			}
		}
		throw new CustomException(ErrorCode.INVALID_INPUT);
	}
}
