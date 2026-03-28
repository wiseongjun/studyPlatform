package com.example.api.problem.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.api.problem.dto.ProblemDetailResponse;
import com.example.api.problem.dto.ProblemSummaryResponse;

@FeignClient(name = "problem-service")
public interface ProblemFeignClient {

	@GetMapping("/internal/v1/problem/list")
	List<ProblemSummaryResponse> getProblemsIncludingDeleted(@RequestParam("problemIds") List<Long> problemIds);

	@GetMapping("/internal/v1/problem/{problemId}/detail")
	ProblemDetailResponse getProblemDetail(@PathVariable("problemId") Long problemId);
}
