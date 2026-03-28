package com.example.problem.repository;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProblemRepository {
	private final JPAQueryFactory queryFactory;
	private final EntityManager em;

}
