package com.example.api.user.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.api.user.dto.SaveAttemptRequest;

@FeignClient(name = "user-service")
public interface UserFeignClient {

	@GetMapping("/internal/v1/user/{userId}/problem/solved/problemIds")
	List<Long> getUserSolvedProblemIdList(
		@PathVariable Long userId,
		@RequestParam Long chapterId
	);

	@PostMapping("/internal/v1/user/problem/attempt")
	void saveAttempt(@RequestBody SaveAttemptRequest request);
}
