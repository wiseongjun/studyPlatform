package com.example.user.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import com.example.user.dto.res.AttemptDetailResponse;
import com.example.user.dto.res.SolvedProblemResponse;
import com.example.user.service.UserService;

@Tag(name = "User", description = "사용자 관련 API")
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@Operation(summary = "풀었던 문제 목록 조회", description = "사용자가 풀었던 문제 목록을 반환합니다.")
	@GetMapping("/{userId}/problem/solved/list")
	public ResponseEntity<List<SolvedProblemResponse>> getSolvedProblemList(@PathVariable Long userId) {
		return ResponseEntity.ok(userService.getSolvedProblemList(userId));
	}

	@Operation(summary = "풀었던 문제 상세 조회", description = "문제 ID 기준 마지막 시도에 대한 상세 정보를 반환합니다. 사용자 답안, 정답, 해설, 정답률을 포함합니다.")
	@GetMapping("/{userId}/problem/solved/{problemId}")
	public ResponseEntity<AttemptDetailResponse> getSolvedProblemDetail(
		@PathVariable Long userId,
		@PathVariable Long problemId
	) {
		return ResponseEntity.ok(userService.getSolvedProblemDetail(problemId, userId));
	}
}
