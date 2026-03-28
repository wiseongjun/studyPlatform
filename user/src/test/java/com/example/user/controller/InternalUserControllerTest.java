package com.example.user.controller;

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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.example.api.user.dto.SaveAttemptRequest;
import com.example.config.GlobalExceptionHandler;
import com.example.constants.AnswerType;
import com.example.exception.ErrorCode;
import com.example.user.service.UserService;

@WebMvcTest(controllers = InternalUserController.class)
@Import(GlobalExceptionHandler.class)
class InternalUserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private UserService userService;

	@Test
	@DisplayName("GET solved/problemIds — chapterId 누락 시 400")
	void getSolvedProblemIds_missingChapterId_returnsBadRequest() throws Exception {
		mockMvc.perform(get("/internal/v1/user/1/problem/solved/problemIds"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errorCode").value(ErrorCode.INVALID_INPUT.getCode()));

		then(userService).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("GET solved/problemIds — 정상 시 200")
	void getSolvedProblemIds_valid_returnsOk() throws Exception {
		given(userService.getSolvedProblemIds(1L, 2L)).willReturn(List.of(10L, 20L));

		mockMvc.perform(get("/internal/v1/user/1/problem/solved/problemIds").param("chapterId", "2"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0]").value(10))
			.andExpect(jsonPath("$[1]").value(20));

		then(userService).should().getSolvedProblemIds(1L, 2L);
	}

	@Test
	@DisplayName("POST problem/attempt — 필수 필드 누락 시 400")
	void saveAttempt_missingFields_returnsBadRequest() throws Exception {
		String json = """
			{"userId":1,"problemId":10}
			""";

		mockMvc.perform(post("/internal/v1/user/problem/attempt")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errorCode").value(ErrorCode.INVALID_INPUT.getCode()));

		then(userService).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("POST problem/attempt — 정상 시 200")
	void saveAttempt_valid_returnsOk() throws Exception {
		SaveAttemptRequest request = new SaveAttemptRequest(
			1L, 10L, 2L, AnswerType.CORRECT, List.of(3), null
		);

		mockMvc.perform(post("/internal/v1/user/problem/attempt")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk());

		then(userService).should().saveAttempt(argThat(r ->
			r.getUserId().equals(1L) && r.getProblemId().equals(10L) && r.getAnswerType() == AnswerType.CORRECT));
	}
}
