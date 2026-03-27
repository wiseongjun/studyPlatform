package com.example.problem.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.example.problem.dto.TestDto;
import com.example.problem.service.ProblemService;

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
