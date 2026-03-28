package com.example.problem.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import com.example.constants.AnswerType;
import com.example.exception.CustomException;
import com.example.exception.ErrorCode;
import com.example.problem.dto.internal.ProblemAnswerDto;
import com.example.problem.dto.internal.ProblemDetailDto;
import com.example.problem.dto.internal.ProblemInfoDto;
import com.example.problem.entity.Problem;
import com.example.problem.entity.ProblemStatus;
import com.example.problem.entity.QProblem;
import com.example.problem.entity.QProblemAnswer;
import com.example.problem.entity.QProblemChoice;
import com.example.problem.entity.QProblemStatus;

@Repository
@RequiredArgsConstructor
public class ProblemRepository {

	private final JPAQueryFactory queryFactory;

	// 문제 추가/삭제 시: @CacheEvict(value = "problemIds", key = "#chapterId") 로 무효화 필요
	@Cacheable(value = "problemIds", key = "#chapterId")
	public List<Long> getProblemIds(Long chapterId) {
		QProblem qProblem = QProblem.problem;
		return queryFactory
			.select(qProblem.id)
			.from(qProblem)
			.where(qProblem.chapterId.eq(chapterId), qProblem.deleted.isFalse())
			.fetch();
	}

	public ProblemInfoDto getProblemInfoById(Long problemId) {
		QProblem qProblem = QProblem.problem;
		QProblemChoice qChoice = QProblemChoice.problemChoice;

		Problem problem = queryFactory
			.selectFrom(qProblem)
			.leftJoin(qProblem.problemStatus).fetchJoin()
			.where(qProblem.id.eq(problemId))
			.fetchOne();

		if (problem == null) {
			throw new CustomException(ErrorCode.PROBLEM_NOT_FOUND);
		}

		List<String> choices = queryFactory
			.select(qChoice.choiceText)
			.from(qChoice)
			.where(qChoice.problemId.eq(problemId))
			.orderBy(qChoice.choiceNumber.asc())
			.fetch();

		ProblemStatus status = problem.getProblemStatus();
		int total = status != null ? status.getTotalAttempts() : 0;
		int correct = status != null ? status.getCorrectAttempts() : 0;

		return new ProblemInfoDto(
			problem.getId(),
			problem.getTitle(),
			problem.getContent(),
			problem.getType(),
			choices,
			total,
			correct
		);
	}

	public ProblemAnswerDto getProblemAnswer(Long problemId) {
		QProblem qProblem = QProblem.problem;
		QProblemAnswer qAnswer = QProblemAnswer.problemAnswer;

		Problem problem = queryFactory
			.selectFrom(qProblem)
			.where(qProblem.id.eq(problemId))
			.fetchOne();

		if (problem == null) {
			throw new CustomException(ErrorCode.PROBLEM_NOT_FOUND);
		}

		List<Integer> correctChoices = queryFactory
			.select(qAnswer.choiceNumber)
			.from(qAnswer)
			.where(qAnswer.problemId.eq(problemId), qAnswer.choiceNumber.isNotNull())
			.fetch();

		String correctTextAnswer = queryFactory
			.select(qAnswer.answerText)
			.from(qAnswer)
			.where(qAnswer.problemId.eq(problemId), qAnswer.answerText.isNotNull())
			.fetchFirst();

		return new ProblemAnswerDto(
			problemId,
			problem.getType(),
			correctChoices,
			correctTextAnswer,
			problem.getExplanation()
		);
	}

	public ProblemDetailDto getProblemDetail(Long problemId) {
		QProblem qProblem = QProblem.problem;
		QProblemAnswer qAnswer = QProblemAnswer.problemAnswer;

		Problem problem = queryFactory
			.selectFrom(qProblem)
			.leftJoin(qProblem.problemStatus).fetchJoin()
			.where(qProblem.id.eq(problemId))
			.fetchOne();

		if (problem == null) {
			throw new CustomException(ErrorCode.PROBLEM_NOT_FOUND);
		}

		List<Integer> correctChoices = queryFactory
			.select(qAnswer.choiceNumber)
			.from(qAnswer)
			.where(qAnswer.problemId.eq(problemId), qAnswer.choiceNumber.isNotNull())
			.fetch();

		String correctTextAnswer = queryFactory
			.select(qAnswer.answerText)
			.from(qAnswer)
			.where(qAnswer.problemId.eq(problemId), qAnswer.answerText.isNotNull())
			.fetchFirst();

		ProblemStatus status = problem.getProblemStatus();
		int total = status != null ? status.getTotalAttempts() : 0;
		int correct = status != null ? status.getCorrectAttempts() : 0;

		return new ProblemDetailDto(
			problem.getId(),
			problem.getTitle(),
			problem.getContent(),
			problem.getExplanation(),
			problem.getType(),
			correctChoices,
			correctTextAnswer,
			total,
			correct
		);
	}

	public List<Problem> findByIds(List<Long> problemIds) {
		QProblem qProblem = QProblem.problem;
		return queryFactory
			.selectFrom(qProblem)
			.leftJoin(qProblem.problemStatus).fetchJoin()
			.where(qProblem.id.in(problemIds))
			.fetch();
	}

	public List<Problem> findByChapterId(Long chapterId) {
		QProblem qProblem = QProblem.problem;
		return queryFactory
			.selectFrom(qProblem)
			.leftJoin(qProblem.problemStatus).fetchJoin()
			.where(qProblem.chapterId.eq(chapterId), qProblem.deleted.isFalse())
			.fetch();
	}

	public Map<Long, List<String>> getChoicesMap(List<Long> problemIds) {
		QProblemChoice qChoice = QProblemChoice.problemChoice;
		Map<Long, List<String>> choicesMap = new HashMap<>();
		List<Tuple> tuples = queryFactory
			.select(qChoice.problemId, qChoice.choiceText)
			.from(qChoice)
			.where(qChoice.problemId.in(problemIds))
			.orderBy(qChoice.choiceNumber.asc())
			.fetch();
		tuples.forEach(tuple -> choicesMap
			.computeIfAbsent(tuple.get(qChoice.problemId), key -> new ArrayList<>())
			.add(tuple.get(qChoice.choiceText)));
		return choicesMap;
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
