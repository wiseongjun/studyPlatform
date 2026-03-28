package com.example.chapter.controller;

import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.example.chapter.dto.req.ChapterListFilter;
import com.example.chapter.dto.res.ChapterListResponse;
import com.example.chapter.service.ChapterService;

@RestController
@RequestMapping("/api/v1/chapter")
@RequiredArgsConstructor
public class ChapterController {

	private final ChapterService chapterService;

	@GetMapping("/list")
	public ResponseEntity<List<ChapterListResponse>> getChapterList(
		@ParameterObject @ModelAttribute @Valid ChapterListFilter filter
	) {
		return ResponseEntity.ok(chapterService.getChapterList(filter));
	}
}
