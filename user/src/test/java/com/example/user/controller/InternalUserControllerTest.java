package com.example.user.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.config.GlobalExceptionHandler;
import com.example.config.PassportAuthenticationFilter;
import com.example.config.SecurityConfig;
import com.example.exception.ErrorCode;
import com.example.user.service.UserService;

@WebMvcTest(controllers = InternalUserController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class, PassportAuthenticationFilter.class})
class InternalUserControllerTest {

	@Autowired
	private MockMvc mockMvc;

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

}
