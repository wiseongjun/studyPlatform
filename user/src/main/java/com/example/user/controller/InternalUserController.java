package com.example.user.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;

import com.example.api.user.dto.SaveAttemptRequest;
import com.example.user.service.UserService;

@Hidden
@RestController
@RequestMapping("/internal/v1/user")
@RequiredArgsConstructor
public class InternalUserController {

	private final UserService userService;

	@GetMapping("/{userId}/problem/solved/problemIds")
	public ResponseEntity<List<Long>> getSolvedProblemIds(
		@PathVariable Long userId,
		@RequestParam @NotNull Long chapterId
	) {
		return ResponseEntity.ok(userService.getSolvedProblemIds(userId, chapterId));
	}

	@PostMapping("/problem/attempt")
	public ResponseEntity<Void> saveAttempt(
		@RequestBody @Valid SaveAttemptRequest request
	) {
		userService.saveAttempt(request);
		return ResponseEntity.ok().build();
	}
}
