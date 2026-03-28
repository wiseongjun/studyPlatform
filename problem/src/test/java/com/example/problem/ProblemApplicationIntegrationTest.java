package com.example.problem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.example.api.user.client.UserFeignClient;
import com.example.api.user.dto.SaveAttemptRequest;
import com.example.constants.AnswerType;
import com.example.problem.dto.req.SubmitProblemRequest;
import com.example.problem.dto.res.ProblemResponse;
import com.example.problem.dto.res.SubmitProblemResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integrationtest")
class ProblemApplicationIntegrationTest {

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private UserFeignClient userFeignClient;

	@BeforeEach
	void stubUserFeign() {
		given(userFeignClient.getUserSolvedProblemIdList(anyLong(), anyLong())).willReturn(List.of());
	}

	@Test
	@DisplayName("GET /api/v1/problem/random — DB·서비스·컨트롤러까지 통합, 삭제 제외 문제만 후보")
	void getRandomProblem_returnsNonDeletedProblemFromChapter() {
		ResponseEntity<ProblemResponse> res = restTemplate.getForEntity(
			"/api/v1/problem/random?userId=1&chapterId=1",
			ProblemResponse.class
		);

		assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(res.getBody()).isNotNull();
		assertThat(res.getBody().getProblemId()).isIn(1L, 2L);
		assertThat(res.getBody().getTitle()).startsWith("Problem ");
	}

	@Test
	@DisplayName("GET /api/v1/problem/list — 챕터별 목록 조회")
	void getProblemListByChapter_returnsProblemsForChapter() {
		ResponseEntity<ProblemResponse[]> res = restTemplate.getForEntity(
			"/api/v1/problem/list?chapterId=1",
			ProblemResponse[].class
		);

		assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(res.getBody()).isNotNull();
		assertThat(res.getBody()).hasSize(2);
		assertThat(res.getBody())
			.extracting(ProblemResponse::getProblemId)
			.containsExactlyInAnyOrder(1L, 2L);
	}

	@Test
	@DisplayName("POST /api/v1/problem/{id}/submit — 정답 시 CORRECT 및 비동기 saveAttempt 호출")
	void submitProblem_correctAnswer_updatesAndCallsUserService() throws Exception {
		SubmitProblemRequest body = new SubmitProblemRequest(1L, List.of(1), null);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String json = objectMapper.writeValueAsString(body);
		HttpEntity<String> entity = new HttpEntity<>(json, headers);

		ResponseEntity<SubmitProblemResponse> res = restTemplate.postForEntity(
			"/api/v1/problem/1/submit",
			entity,
			SubmitProblemResponse.class
		);

		assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(res.getBody()).isNotNull();
		assertThat(res.getBody().getAnswerStatus()).isEqualTo(AnswerType.CORRECT);
		assertThat(res.getBody().getProblemId()).isEqualTo(1L);

		verify(userFeignClient, timeout(5000)).saveAttempt(argThat((SaveAttemptRequest r) ->
			r.getUserId().equals(1L)
				&& r.getProblemId().equals(1L)
				&& r.getChapterId().equals(1L)
				&& r.getAnswerType() == AnswerType.CORRECT));
	}

	@Test
	@DisplayName("POST /api/v1/problem/{id}/submit — 오답 시 INCORRECT")
	void submitProblem_wrongAnswer_returnsIncorrect() throws Exception {
		SubmitProblemRequest body = new SubmitProblemRequest(1L, List.of(3), null);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String json = objectMapper.writeValueAsString(body);
		HttpEntity<String> entity = new HttpEntity<>(json, headers);

		ResponseEntity<SubmitProblemResponse> res = restTemplate.postForEntity(
			"/api/v1/problem/1/submit",
			entity,
			SubmitProblemResponse.class
		);

		assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(res.getBody()).isNotNull();
		assertThat(res.getBody().getAnswerStatus()).isEqualTo(AnswerType.INCORRECT);
	}
}
