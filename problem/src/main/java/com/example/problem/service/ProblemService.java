package com.example.problem.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.example.problem.repository.ProblemRepository;

@Service
@RequiredArgsConstructor
public class ProblemService {
	private final ProblemRepository problemRepository;

}
