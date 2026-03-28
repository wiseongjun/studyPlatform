package com.example.chapter.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.example.type.ChapterCategory;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChapterListCondition {
	private String name;
	private ChapterCategory category;
}
