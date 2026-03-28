package com.example.chapter.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.example.chapter.dto.ChapterResponse;
import com.example.chapter.service.ChapterService;

@RestController
@RequestMapping("/api/v1/chapter")
@RequiredArgsConstructor
public class ChapterController {

	private final ChapterService chapterService;

	@GetMapping("/list")
	public ResponseEntity<List<ChapterResponse>> list() {
		return ResponseEntity.ok(chapterService.getChapterList());
	}
}
