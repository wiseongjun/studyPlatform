package com.example.chapter.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.example.chapter.dto.req.ChapterListFilter;
import com.example.chapter.dto.res.ChapterListResponse;
import com.example.chapter.repository.ChapterRepository;

@Service
@RequiredArgsConstructor
public class ChapterService {

	private final ChapterRepository chapterRepository;

	public List<ChapterListResponse> getChapterList(ChapterListFilter filter) {
		return chapterRepository.getChapterList(filter.toCondition());
	}
}
