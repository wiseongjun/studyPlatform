package com.example.chapter.dto.res;

import lombok.Getter;
import lombok.NoArgsConstructor;

import com.example.type.ChapterCategory;

@Getter
@NoArgsConstructor
public class ChapterListResponse {
	private Long id;
	private String name;
	private ChapterCategory category;

	public ChapterListResponse(Long id, String name, ChapterCategory category) {
		this.id = id;
		this.name = name;
		this.category = category;
	}
}
