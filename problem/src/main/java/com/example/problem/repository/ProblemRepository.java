package com.example.problem.repository;

import com.example.problem.dto.TestDto;
import com.example.problem.entity.QTestEntity;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.problem.entity.QTestEntity.testEntity;

@Repository
@RequiredArgsConstructor
public class ProblemRepository {
	private final JPAQueryFactory queryFactory;
	private final EntityManager em;

	public List<TestDto> getTest() {
		return queryFactory
			.select(Projections.constructor(TestDto.class,
				testEntity.id,
				testEntity.value
			))
			.from(testEntity)
			.fetch();
	}
}
