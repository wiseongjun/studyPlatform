package com.example.problem.controller;

import com.example.problem.dto.TestDto;
import com.example.problem.service.ProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/problem")
@RequiredArgsConstructor
public class ProblemController {
	private final ProblemService problemService;

	@GetMapping("/test")
	public ResponseEntity<List<TestDto>> list() {
		return ResponseEntity.ok(problemService.getTest());
	}
}
