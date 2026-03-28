package com.example.problem.repository;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import com.example.api.problem.dto.ProblemSummaryResponse;
import com.example.constants.AnswerType;
import com.example.problem.dto.internal.ProblemAnswerDto;
import com.example.problem.dto.internal.ProblemDetailDto;
import com.example.problem.dto.res.ProblemResponse;
import com.example.problem.entity.ProblemStatus;
import com.example.problem.entity.QProblemStatus;

@Repository
@RequiredArgsConstructor
public class ProblemRepository {

	private final JPAQueryFactory queryFactory;

	// 문제 추가/삭제 시: @CacheEvict(value = "problemIds", key = "#chapterId") 로 무효화 필요
	@Cacheable(value = "problemIds", key = "#chapterId")
	public List<Long> getProblemIds(Long chapterId) {
		// TODO: T_PROBLEM에서 chapterId 기준 미삭제 문제 ID 목록 조회
		return null;
	}

	public ProblemResponse getProblemInfoById(Long problemId) {
		// TODO: T_PROBLEM + T_PROBLEM_CHOICE + T_PROBLEM_STATUS 조인 조회 후 ProblemResponse 변환
		return null;
	}

	public ProblemAnswerDto getProblemAnswer(Long problemId) {
		// TODO: T_PROBLEM + T_PROBLEM_ANSWER 조인 조회
		return null;
	}

	public ProblemDetailDto getProblemDetail(Long problemId) {
		// TODO: T_PROBLEM + T_PROBLEM_ANSWER + T_PROBLEM_STATUS 조인 조회
		return null;
	}

	public List<ProblemSummaryResponse> getProblemsIncludingDeleted(List<Long> problemIds) {
		// TODO: T_PROBLEM + T_PROBLEM_CHOICE + T_PROBLEM_STATUS IN 조회 (삭제 여부 필터 없음)
		return null;
	}

	public ProblemStatus getProblemStatus(Long problemId) {
		QProblemStatus qProblemStatus = QProblemStatus.problemStatus;
		return queryFactory
			.selectFrom(qProblemStatus)
			.where(qProblemStatus.problem.id.eq(problemId))
			.fetchOne();
	}

	// 동시성 이슈 방지: SELECT → UPDATE 대신 DB 레벨 atomic update 사용
	// dirty checking으로 하면 동시 요청 시 totalAttempts 유실 가능
	public void incrementAttemptCounts(Long problemId, AnswerType answerType) {
		QProblemStatus qStatus = QProblemStatus.problemStatus;
		queryFactory.update(qStatus)
			.set(qStatus.totalAttempts, qStatus.totalAttempts.add(1))
			.set(qStatus.correctAttempts,
				answerType == AnswerType.CORRECT
					? qStatus.correctAttempts.add(1)
					: qStatus.correctAttempts)
			.where(qStatus.problem.id.eq(problemId))
			.execute();
	}
}
