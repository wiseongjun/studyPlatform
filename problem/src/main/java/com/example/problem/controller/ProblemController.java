package com.example.problem.controller;

import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

import com.example.problem.dto.req.RandomProblemFilter;
import com.example.problem.dto.req.SubmitProblemRequest;
import com.example.problem.dto.res.ProblemResponse;
import com.example.problem.dto.res.SubmitProblemResponse;
import com.example.problem.service.ProblemService;

@Tag(name = "Problem", description = "문제 관련 API")
@RestController
@RequestMapping("/api/v1/problem")
@RequiredArgsConstructor
public class ProblemController {

	private final ProblemService problemService;

	@Operation(summary = "랜덤 문제 조회", description = "사용자가 풀지 않은 문제를 랜덤으로 1개 반환합니다. 문제 넘기기 시 동일 API를 재호출합니다.")
	@GetMapping("/random")
	public ResponseEntity<ProblemResponse> getRandomProblem(
		@ParameterObject @ModelAttribute @Valid RandomProblemFilter filter
	) {
		return ResponseEntity.ok(problemService.getRandomProblem(filter));
	}

	@Operation(summary = "문제 제출", description = "사용자의 답안을 제출하고 정답 여부와 해설을 반환합니다.")
	@PostMapping("/{problemId}/submit")
	public ResponseEntity<SubmitProblemResponse> submitProblem(
		@PathVariable Long problemId,
		@RequestBody @Valid SubmitProblemRequest request
	) {
		return ResponseEntity.ok(problemService.submitProblem(problemId, request));
	}

	@Operation(summary = "챕터별 문제 목록 조회", description = "특정 챕터의 전체 문제 목록을 반환합니다.")
	@GetMapping("/list")
	public ResponseEntity<List<ProblemResponse>> getProblemListByChapter(
		@RequestParam @NotNull Long chapterId
	) {
		return ResponseEntity.ok(problemService.getProblemListByChapter(chapterId));
	}
}
