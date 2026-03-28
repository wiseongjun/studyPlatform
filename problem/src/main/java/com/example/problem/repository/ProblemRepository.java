package com.example.problem.repository;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import com.example.exception.CustomException;
import com.example.exception.ErrorCode;
import com.example.problem.entity.Problem;
import com.example.problem.entity.QProblem;
import com.example.problem.repository.support.ProblemQuerySupport;

@Repository
@RequiredArgsConstructor
public class ProblemRepository {

	private final JPAQueryFactory queryFactory;

	// TODO: 문제 추가/삭제 기능 구현 시 @CacheEvict(value = "problemIds", key = "#chapterId") 적용 필요
	//       미적용 시 TTL(30분) 동안 stale 데이터 서빙 → 삭제된 문제 출제 or 신규 문제 미출제
	@Cacheable(value = "problemIds", key = "#chapterId")
	public List<Long> getProblemIds(Long chapterId) {
		QProblem qProblem = QProblem.problem;
		return queryFactory
			.select(qProblem.id)
			.from(qProblem)
			.where(ProblemQuerySupport.problemChapterIdEq(chapterId), ProblemQuerySupport.problemNotDeleted())
			.fetch();
	}

	public Problem findByIdWithChoices(Long problemId) {
		QProblem qProblem = QProblem.problem;
		Problem problem = queryFactory
			.selectFrom(qProblem)
			.leftJoin(qProblem.choices).fetchJoin()
			.leftJoin(qProblem.problemStatus).fetchJoin()
			.where(ProblemQuerySupport.problemIdEq(problemId))
			.fetchOne();

		if (problem == null) {
			throw new CustomException(ErrorCode.PROBLEM_NOT_FOUND);
		}
		return problem;
	}

	public Problem findByIdWithAnswers(Long problemId) {
		QProblem qProblem = QProblem.problem;
		Problem problem = queryFactory
			.selectFrom(qProblem)
			.leftJoin(qProblem.answers).fetchJoin()
			.where(ProblemQuerySupport.problemIdEq(problemId))
			.fetchOne();

		if (problem == null) {
			throw new CustomException(ErrorCode.PROBLEM_NOT_FOUND);
		}
		return problem;
	}

	public Problem findByIdWithAnswersAndStatus(Long problemId) {
		QProblem qProblem = QProblem.problem;
		Problem problem = queryFactory
			.selectFrom(qProblem)
			.leftJoin(qProblem.answers).fetchJoin()
			.leftJoin(qProblem.problemStatus).fetchJoin()
			.where(ProblemQuerySupport.problemIdEq(problemId))
			.fetchOne();

		if (problem == null) {
			throw new CustomException(ErrorCode.PROBLEM_NOT_FOUND);
		}
		return problem;
	}

	public List<Problem> findByIdsWithChoices(List<Long> problemIds) {
		if (problemIds == null || problemIds.isEmpty()) {
			return List.of();
		}
		QProblem qProblem = QProblem.problem;
		return queryFactory
			.selectFrom(qProblem).distinct()
			.leftJoin(qProblem.choices).fetchJoin()
			.leftJoin(qProblem.problemStatus).fetchJoin()
			.where(ProblemQuerySupport.problemIdIn(problemIds))
			.fetch();
	}

	public List<Problem> findByChapterIdWithChoices(Long chapterId) {
		QProblem qProblem = QProblem.problem;
		return queryFactory
			.selectFrom(qProblem).distinct()
			.leftJoin(qProblem.choices).fetchJoin()
			.leftJoin(qProblem.problemStatus).fetchJoin()
			.where(ProblemQuerySupport.problemChapterIdEq(chapterId), ProblemQuerySupport.problemNotDeleted())
			.fetch();
	}
}
