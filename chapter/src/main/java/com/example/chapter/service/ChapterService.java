package com.example.chapter.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.example.chapter.dto.ChapterResponse;
import com.example.chapter.repository.ChapterRepository;

@Service
@RequiredArgsConstructor
public class ChapterService {

	private final ChapterRepository chapterRepository;

	public List<ChapterResponse> getChapterList() {
		return chapterRepository.getChapterList();
	}
}
