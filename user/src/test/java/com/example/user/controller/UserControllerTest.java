package com.example.user.controller;

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

import com.example.config.GlobalExceptionHandler;
import com.example.exception.CustomException;
import com.example.exception.ErrorCode;
import com.example.user.service.UserService;

@WebMvcTest(controllers = UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserService userService;

	@Test
	@DisplayName("getSolvedProblemDetail — 시도 기록이 없으면 404")
	void getSolvedProblemDetail_noAttempt_returnsNotFound() throws Exception {
		given(userService.getSolvedProblemDetail(10L, 1L)).willThrow(new CustomException(ErrorCode.ENTITY_NOT_FOUND));

		mockMvc.perform(get("/api/v1/user/1/problem/solved/10"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.errorCode").value(ErrorCode.ENTITY_NOT_FOUND.getCode()));
	}

	@Test
	@DisplayName("GET 풀이 목록 — 정상 시 200")
	void getSolvedProblemList_returnsOk() throws Exception {
		given(userService.getSolvedProblemList(1L)).willReturn(List.of());

		mockMvc.perform(get("/api/v1/user/1/problem/solved/list"))
			.andExpect(status().isOk())
			.andExpect(content().json("[]"));

		then(userService).should().getSolvedProblemList(1L);
	}
}
