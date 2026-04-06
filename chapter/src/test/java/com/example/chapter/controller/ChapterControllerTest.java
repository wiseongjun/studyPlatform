package com.example.chapter.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.chapter.service.ChapterService;
import com.example.config.GlobalExceptionHandler;
import com.example.config.PassportAuthenticationFilter;
import com.example.config.SecurityConfig;
import com.example.exception.ErrorCode;

@WebMvcTest(controllers = ChapterController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class, PassportAuthenticationFilter.class})
@WithMockUser
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

}
