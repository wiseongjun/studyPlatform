package com.example.chapter.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.example.chapter.dto.internal.ChapterListCondition;
import com.example.constants.ChapterCategory;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChapterListFilter {

	@Schema(description = "단원 이름 검색어", example = "자바")
	@Size(max = 100, message = "이름 검색어는 100자 이하여야 합니다.")
	private String name;

	@Schema(description = "단원 카테고리", example = "JAVA")
	private ChapterCategory category;

	public ChapterListCondition toCondition() {
		return new ChapterListCondition(name, category);
	}
}
