package com.example.problem.service;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.example.problem.dto.TestDto;
import com.example.problem.repository.ProblemRepository;

@Service
@RequiredArgsConstructor
public class ProblemService {
	private final ProblemRepository problemRepository;

	@Cacheable(value = "test", key = "'getTest'")
	public List<TestDto> getTest() {
		return problemRepository.getTest();
	}
}
