package com.example.problem.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.example.api.user.client.UserFeignClient;
import com.example.api.user.dto.SaveAttemptRequest;

@Service
@RequiredArgsConstructor
public class ProblemAsyncService {

	private final UserFeignClient userFeignClient;

	@Async
	public void saveAttempt(SaveAttemptRequest request) {
		userFeignClient.saveAttempt(request);
	}
}