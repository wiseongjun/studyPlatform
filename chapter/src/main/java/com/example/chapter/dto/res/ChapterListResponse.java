package com.example.chapter.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.example.constants.ChapterCategory;

@Schema(description = "단원 목록 조회 응답")
@Getter
@NoArgsConstructor
public class ChapterListResponse {

	@Schema(description = "단원 ID", example = "1")
	private Long id;

	@Schema(description = "단원 이름", example = "자바 기초")
	private String name;

	@Schema(description = "단원 카테고리")
	private ChapterCategory category;

	public ChapterListResponse(Long id, String name, ChapterCategory category) {
		this.id = id;
		this.name = name;
		this.category = category;
	}
}
