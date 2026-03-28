package com.example.problem.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;

import com.example.api.problem.dto.ProblemDetailResponse;
import com.example.api.problem.dto.ProblemSummaryResponse;
import com.example.problem.service.ProblemService;

@Hidden
@RestController
@RequestMapping("/internal/v1/problem")
@RequiredArgsConstructor
public class InternalProblemController {

	private final ProblemService problemService;

	@GetMapping("/list")
	public ResponseEntity<List<ProblemSummaryResponse>> getProblemsIncludingDeleted(
		@RequestParam List<Long> problemIds
	) {
		return ResponseEntity.ok(problemService.getProblemsIncludingDeleted(problemIds));
	}

	@GetMapping("/{problemId}/detail")
	public ResponseEntity<ProblemDetailResponse> getProblemDetail(@PathVariable Long problemId) {
		return ResponseEntity.ok(problemService.getProblemDetail(problemId));
	}
}
