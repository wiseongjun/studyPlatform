package com.example.chapter.controller;

import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.example.chapter.dto.req.ChapterListFilter;
import com.example.chapter.dto.res.ChapterListResponse;
import com.example.chapter.service.ChapterService;

@Tag(name = "Chapter", description = "단원 관련 API")
@RestController
@RequestMapping("/api/v1/chapter")
@RequiredArgsConstructor
public class ChapterController {

	private final ChapterService chapterService;

	@Operation(summary = "단원 목록 조회", description = "이름 또는 카테고리로 필터링하여 단원 목록을 반환합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "단원 목록 조회 성공")
	})
	@GetMapping("/list")
	public ResponseEntity<List<ChapterListResponse>> getChapterList(
		@ParameterObject @ModelAttribute @Valid ChapterListFilter filter
	) {
		return ResponseEntity.ok(chapterService.getChapterList(filter));
	}
}
