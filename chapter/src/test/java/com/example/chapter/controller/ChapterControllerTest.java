package com.example.chapter.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.chapter.dto.res.ChapterListResponse;
import com.example.chapter.service.ChapterService;
import com.example.config.GlobalExceptionHandler;
import com.example.constants.ChapterCategory;
import com.example.exception.ErrorCode;

@WebMvcTest(controllers = ChapterController.class)
@Import(GlobalExceptionHandler.class)
class ChapterControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ChapterService chapterService;

	@Test
	@DisplayName("GET /list — name이 100자 초과면 400")
	void getChapterList_nameTooLong_returnsBadRequest() throws Exception {
		String longName = "x".repeat(101);

		mockMvc.perform(get("/api/v1/chapter/list").param("name", longName))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errorCode").value(ErrorCode.INVALID_INPUT.getCode()));

		then(chapterService).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("GET /list — 정상 시 200 및 서비스 호출")
	void getChapterList_valid_returnsOk() throws Exception {
		ChapterListResponse row = new ChapterListResponse(1L, "자바", ChapterCategory.JAVA);
		given(chapterService.getChapterList(any())).willReturn(List.of(row));

		mockMvc.perform(get("/api/v1/chapter/list").param("name", "자바"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].id").value(1))
			.andExpect(jsonPath("$[0].name").value("자바"));

		then(chapterService).should().getChapterList(argThat(f -> "자바".equals(f.getName())));
	}
}
