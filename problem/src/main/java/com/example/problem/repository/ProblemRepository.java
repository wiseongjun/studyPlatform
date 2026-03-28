package com.example.problem.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

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
import com.example.problem.repository.support.ProblemQuerySupport;

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
			.where(ProblemQuerySupport.problemChapterIdEq(chapterId), ProblemQuerySupport.problemNotDeleted())
			.fetch();
	}

	public ProblemInfoDto getProblemInfoById(Long problemId) {
		QProblem qProblem = QProblem.problem;
		QProblemChoice qChoice = QProblemChoice.problemChoice;

		Problem problem = queryFactory
			.selectFrom(qProblem)
			.leftJoin(qProblem.problemStatus).fetchJoin()
			.where(ProblemQuerySupport.problemIdEq(problemId))
			.fetchOne();

		if (problem == null) {
			throw new CustomException(ErrorCode.PROBLEM_NOT_FOUND);
		}

		List<String> choices = queryFactory
			.select(qChoice.choiceText)
			.from(qChoice)
			.where(ProblemQuerySupport.choiceProblemIdEq(problemId))
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

		Problem problem = queryFactory
			.selectFrom(qProblem)
			.where(ProblemQuerySupport.problemIdEq(problemId))
			.fetchOne();

		if (problem == null) {
			throw new CustomException(ErrorCode.PROBLEM_NOT_FOUND);
		}

		AnswerParts answers = fetchAnswerParts(problemId);

		return new ProblemAnswerDto(
			problemId,
			problem.getType(),
			answers.correctChoices(),
			answers.correctTextAnswer(),
			problem.getExplanation()
		);
	}

	public ProblemDetailDto getProblemDetail(Long problemId) {
		QProblem qProblem = QProblem.problem;

		Problem problem = queryFactory
			.selectFrom(qProblem)
			.leftJoin(qProblem.problemStatus).fetchJoin()
			.where(ProblemQuerySupport.problemIdEq(problemId))
			.fetchOne();

		if (problem == null) {
			throw new CustomException(ErrorCode.PROBLEM_NOT_FOUND);
		}

		AnswerParts answers = fetchAnswerParts(problemId);

		ProblemStatus status = problem.getProblemStatus();
		int total = status != null ? status.getTotalAttempts() : 0;
		int correct = status != null ? status.getCorrectAttempts() : 0;

		return new ProblemDetailDto(
			problem.getId(),
			problem.getTitle(),
			problem.getContent(),
			problem.getExplanation(),
			problem.getType(),
			answers.correctChoices(),
			answers.correctTextAnswer(),
			total,
			correct
		);
	}

	public List<Problem> findByIds(List<Long> problemIds) {
		if (problemIds == null || problemIds.isEmpty()) {
			return List.of();
		}
		QProblem qProblem = QProblem.problem;
		return queryFactory
			.selectFrom(qProblem)
			.leftJoin(qProblem.problemStatus).fetchJoin()
			.where(ProblemQuerySupport.problemIdIn(problemIds))
			.fetch();
	}

	public List<Problem> findByChapterId(Long chapterId) {
		QProblem qProblem = QProblem.problem;
		return queryFactory
			.selectFrom(qProblem)
			.leftJoin(qProblem.problemStatus).fetchJoin()
			.where(ProblemQuerySupport.problemChapterIdEq(chapterId), ProblemQuerySupport.problemNotDeleted())
			.fetch();
	}

	public Map<Long, List<String>> getChoicesMap(List<Long> problemIds) {
		if (problemIds == null || problemIds.isEmpty()) {
			return Collections.emptyMap();
		}
		QProblemChoice qChoice = QProblemChoice.problemChoice;
		Map<Long, List<String>> choicesMap = new HashMap<>();
		List<Tuple> tuples = queryFactory
			.select(qChoice.problemId, qChoice.choiceText)
			.from(qChoice)
			.where(ProblemQuerySupport.choiceProblemIdIn(problemIds))
			.orderBy(qChoice.choiceNumber.asc())
			.fetch();
		tuples.forEach(tuple -> choicesMap
			.computeIfAbsent(tuple.get(qChoice.problemId), key -> new ArrayList<>())
			.add(tuple.get(qChoice.choiceText)));
		return choicesMap;
	}

	private AnswerParts fetchAnswerParts(Long problemId) {
		QProblemAnswer qAnswer = QProblemAnswer.problemAnswer;
		List<Tuple> rows = queryFactory
			.select(qAnswer.choiceNumber, qAnswer.answerText)
			.from(qAnswer)
			.where(ProblemQuerySupport.answerProblemIdEq(problemId))
			.orderBy(qAnswer.id.asc())
			.fetch();
		List<Integer> correctChoices = new ArrayList<>();
		String correctTextAnswer = null;
		for (Tuple tuple : rows) {
			Integer choiceNumber = tuple.get(qAnswer.choiceNumber);
			if (choiceNumber != null) {
				correctChoices.add(choiceNumber);
			}
			String answerText = tuple.get(qAnswer.answerText);
			if (answerText != null && correctTextAnswer == null) {
				correctTextAnswer = answerText;
			}
		}
		return new AnswerParts(correctChoices, correctTextAnswer);
	}

	private record AnswerParts(List<Integer> correctChoices, String correctTextAnswer) {
	}
}
