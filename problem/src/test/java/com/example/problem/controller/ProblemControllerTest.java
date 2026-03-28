package com.example.problem.controller;

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

import com.example.config.GlobalExceptionHandler;
import com.example.constants.AnswerType;
import com.example.constants.ProblemType;
import com.example.exception.CustomException;
import com.example.exception.ErrorCode;
import com.example.problem.dto.req.SubmitProblemRequest;
import com.example.problem.dto.res.ProblemResponse;
import com.example.problem.dto.res.SubmitProblemResponse;
import com.example.problem.service.ProblemService;

@WebMvcTest(controllers = ProblemController.class)
@Import(GlobalExceptionHandler.class)
class ProblemControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private ProblemService problemService;

	@Test
	@DisplayName("GET /random — userId 누락 시 400")
	void getRandomProblem_missingUserId_returnsBadRequest() throws Exception {
		mockMvc.perform(get("/api/v1/problem/random").param("chapterId", "1"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errorCode").value(ErrorCode.INVALID_INPUT.getCode()));
	}

	@Test
	@DisplayName("GET /random — chapterId 누락 시 400")
	void getRandomProblem_missingChapterId_returnsBadRequest() throws Exception {
		mockMvc.perform(get("/api/v1/problem/random").param("userId", "1"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errorCode").value(ErrorCode.INVALID_INPUT.getCode()));
	}

	@Test
	@DisplayName("GET /random — 정상 시 200 및 서비스 응답")
	void getRandomProblem_validParams_returnsOk() throws Exception {
		ProblemResponse body = new ProblemResponse(1L, "t", "c", ProblemType.SINGLE_CHOICE, List.of("A"), null);
		given(problemService.getRandomProblem(any())).willReturn(body);

		mockMvc.perform(get("/api/v1/problem/random").param("userId", "1").param("chapterId", "2"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.problemId").value(1))
			.andExpect(jsonPath("$.title").value("t"));

		then(problemService).should().getRandomProblem(argThat(f ->
			f.getUserId().equals(1L) && f.getChapterId().equals(2L)));
	}

	@Test
	@DisplayName("POST /submit — 필수 필드 누락 시 400")
	void submitProblem_missingRequiredFields_returnsBadRequest() throws Exception {
		String json = """
			{}
			""";

		mockMvc.perform(post("/api/v1/problem/5/submit")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errorCode").value(ErrorCode.INVALID_INPUT.getCode()));
	}

	@Test
	@DisplayName("POST /submit — 정상 시 200 및 서비스 호출")
	void submitProblem_validBody_returnsOk() throws Exception {
		SubmitProblemResponse response = new SubmitProblemResponse(
			5L, AnswerType.CORRECT, "해설", List.of(2), null
		);
		given(problemService.submitProblem(eq(5L), any())).willReturn(response);

		SubmitProblemRequest request = new SubmitProblemRequest(1L, List.of(2), null);

		mockMvc.perform(post("/api/v1/problem/5/submit")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.problemId").value(5))
			.andExpect(jsonPath("$.answerStatus").value("CORRECT"));

		then(problemService).should().submitProblem(eq(5L), argThat(r ->
			r.getUserId().equals(1L)));
	}

	@Test
	@DisplayName("GET /random — 풀 수 있는 문제가 없으면 404")
	void getRandomProblem_noProblemAvailable_returnsNotFound() throws Exception {
		given(problemService.getRandomProblem(any())).willThrow(new CustomException(ErrorCode.NO_PROBLEM_AVAILABLE));

		mockMvc.perform(get("/api/v1/problem/random").param("userId", "1").param("chapterId", "1"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.errorCode").value(ErrorCode.NO_PROBLEM_AVAILABLE.getCode()));
	}

	@Test
	@DisplayName("POST /submit — 문제가 없으면 404")
	void submitProblem_problemNotFound_returnsNotFound() throws Exception {
		given(problemService.submitProblem(eq(9L), any())).willThrow(new CustomException(ErrorCode.PROBLEM_NOT_FOUND));

		SubmitProblemRequest request = new SubmitProblemRequest(1L, List.of(1), null);

		mockMvc.perform(post("/api/v1/problem/9/submit")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.errorCode").value(ErrorCode.PROBLEM_NOT_FOUND.getCode()));
	}
}
