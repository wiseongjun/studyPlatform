package com.example.user;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.example.api.problem.client.ProblemFeignClient;
import com.example.api.user.dto.SaveAttemptRequest;
import com.example.constants.AnswerType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integrationtest")
class UserDetailApplicationIntegrationTest {

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private ProblemFeignClient problemFeignClient;

	@Test
	@DisplayName("GET /internal/v1/user/{userId}/problem/solved/problemIds — DB·리포지토리·컨트롤러 통합")
	void getSolvedProblemIds_returnsDistinctIdsForChapter() {
		ResponseEntity<List<Long>> res = restTemplate.exchange(
			"/internal/v1/user/1/problem/solved/problemIds?chapterId=1",
			HttpMethod.GET,
			null,
			new ParameterizedTypeReference<List<Long>>() {
			}
		);

		assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(res.getBody()).containsExactlyInAnyOrder(10L, 20L);
	}

	@Test
	@DisplayName("POST /internal/v1/user/problem/attempt — 시도·답안 저장 후 200")
	void saveAttempt_persistsAndReturnsOk() throws Exception {
		SaveAttemptRequest body = new SaveAttemptRequest(
			1L,
			99L,
			1L,
			AnswerType.CORRECT,
			List.of(1, 2),
			null
		);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String json = objectMapper.writeValueAsString(body);
		HttpEntity<String> entity = new HttpEntity<>(json, headers);

		ResponseEntity<Void> res = restTemplate.postForEntity(
			"/internal/v1/user/problem/attempt",
			entity,
			Void.class
		);

		assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);

		ResponseEntity<List<Long>> after = restTemplate.exchange(
			"/internal/v1/user/1/problem/solved/problemIds?chapterId=1",
			HttpMethod.GET,
			null,
			new ParameterizedTypeReference<List<Long>>() {
			}
		);
		assertThat(after.getBody()).isNotNull();
		assertThat(after.getBody()).contains(99L);
	}
}
