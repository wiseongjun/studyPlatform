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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

import com.example.problem.dto.req.RandomProblemFilter;
import com.example.problem.dto.req.SubmitProblemRequest;
import com.example.problem.dto.res.ProblemResponse;
import com.example.problem.dto.res.SubmitProblemResponse;
import com.example.problem.service.ProblemService;
import com.example.response.ExceptionResponse;

@Tag(name = "Problem", description = "문제 관련 API")
@RestController
@RequestMapping("/api/v1/problem")
@RequiredArgsConstructor
public class ProblemController {

	private final ProblemService problemService;

	@Operation(summary = "랜덤 문제 조회", description = "사용자가 풀지 않은 문제를 랜덤으로 1개 반환합니다. 문제 넘기기 시 동일 API를 재호출합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "랜덤 문제 조회 성공"),
		@ApiResponse(responseCode = "400", description = "필수 파라미터 누락",
			content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
		@ApiResponse(responseCode = "404", description = "풀 수 있는 문제가 없음",
			content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
	})
	@GetMapping("/random")
	public ResponseEntity<ProblemResponse> getRandomProblem(
		@ParameterObject @ModelAttribute @Valid RandomProblemFilter filter
	) {
		return ResponseEntity.ok(problemService.getRandomProblem(filter));
	}

	@Operation(summary = "문제 제출", description = "사용자의 답안을 제출하고 정답 여부와 해설을 반환합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "문제 제출 성공"),
		@ApiResponse(responseCode = "400", description = "올바르지 않은 입력",
			content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
		@ApiResponse(responseCode = "404", description = "존재하지 않는 문제",
			content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
	})
	@PostMapping("/{problemId}/submit")
	public ResponseEntity<SubmitProblemResponse> submitProblem(
		@Parameter(description = "문제 ID", example = "1") @PathVariable Long problemId,
		@RequestBody @Valid SubmitProblemRequest request
	) {
		return ResponseEntity.ok(problemService.submitProblem(problemId, request));
	}

	@Operation(summary = "챕터별 문제 목록 조회", description = "특정 챕터의 전체 문제 목록을 반환합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "문제 목록 조회 성공"),
		@ApiResponse(responseCode = "400", description = "필수 파라미터 누락",
			content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
	})
	@GetMapping("/list")
	public ResponseEntity<List<ProblemResponse>> getProblemListByChapter(
		@Parameter(description = "챕터 ID", example = "1") @RequestParam @NotNull Long chapterId
	) {
		return ResponseEntity.ok(problemService.getProblemListByChapter(chapterId));
	}
}
