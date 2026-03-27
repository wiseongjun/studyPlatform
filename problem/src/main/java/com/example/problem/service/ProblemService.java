package com.example.problem.service;

import com.example.problem.dto.TestDto;
import com.example.problem.repository.ProblemRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProblemService {
	private final ProblemRepository problemRepository;

	@Cacheable(value = "test", key = "'getTest'")
	public List<TestDto> getTest() {
		return problemRepository.getTest();
	}
}
