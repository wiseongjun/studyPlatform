package com.example.problem.entity;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.constants.AnswerType;
import com.example.constants.ProblemType;
import com.example.exception.CustomException;

class ProblemGradeTest {

	private Problem createProblem(ProblemType type, List<ProblemAnswer> answers) {
		Problem problem = new Problem();
		ReflectionTestUtils.setField(problem, "type", type);
		ReflectionTestUtils.setField(problem, "answers", answers);
		return problem;
	}

	private ProblemAnswer choiceAnswer(Integer choiceNumber) {
		ProblemAnswer answer = new ProblemAnswer();
		ReflectionTestUtils.setField(answer, "choiceNumber", choiceNumber);
		return answer;
	}

	private ProblemAnswer textAnswer(String text) {
		ProblemAnswer answer = new ProblemAnswer();
		ReflectionTestUtils.setField(answer, "answerText", text);
		return answer;
	}

	@Nested
	@DisplayName("단일 정답 (SINGLE_CHOICE)")
	class SingleChoice {

		private final Problem problem = createProblem(
			ProblemType.SINGLE_CHOICE, List.of(choiceAnswer(2)));

		@Test
		@DisplayName("정답 선택지를 고르면 CORRECT를 반환한다")
		void correct() {
			assertThat(problem.grade(List.of(2), null)).isEqualTo(AnswerType.CORRECT);
		}

		@Test
		@DisplayName("오답 선택지를 고르면 INCORRECT를 반환한다")
		void incorrect() {
			assertThat(problem.grade(List.of(1), null)).isEqualTo(AnswerType.INCORRECT);
		}

		@Test
		@DisplayName("선택지가 비어있으면 예외가 발생한다")
		void emptySelected() {
			assertThatThrownBy(() -> problem.grade(List.of(), null))
				.isInstanceOf(CustomException.class);
		}

		@Test
		@DisplayName("선택지가 null이면 예외가 발생한다")
		void nullSelected() {
			assertThatThrownBy(() -> problem.grade(null, null))
				.isInstanceOf(CustomException.class);
		}
	}

	@Nested
	@DisplayName("복수 정답 (MULTI_CHOICE)")
	class MultiChoice {

		private final Problem problem = createProblem(
			ProblemType.MULTI_CHOICE, List.of(choiceAnswer(1), choiceAnswer(2)));

		@Test
		@DisplayName("정답 선택지를 모두 고르면 CORRECT를 반환한다")
		void allCorrect() {
			assertThat(problem.grade(List.of(1, 2), null)).isEqualTo(AnswerType.CORRECT);
		}

		@Test
		@DisplayName("정답 일부와 오답을 함께 선택하면 PARTIAL_CORRECT를 반환한다")
		void partialWithWrong() {
			assertThat(problem.grade(List.of(1, 3), null)).isEqualTo(AnswerType.PARTIAL_CORRECT);
		}

		@Test
		@DisplayName("정답을 모두 포함하더라도 오답이 있으면 PARTIAL_CORRECT를 반환한다")
		void allCorrectPlusWrong() {
			assertThat(problem.grade(List.of(1, 2, 3), null)).isEqualTo(AnswerType.PARTIAL_CORRECT);
		}

		@Test
		@DisplayName("정답을 하나도 선택하지 않으면 INCORRECT를 반환한다")
		void noneCorrect() {
			assertThat(problem.grade(List.of(3, 4), null)).isEqualTo(AnswerType.INCORRECT);
		}

		@Test
		@DisplayName("선택지가 null이면 예외가 발생한다")
		void nullSelected() {
			assertThatThrownBy(() -> problem.grade(null, null))
				.isInstanceOf(CustomException.class);
		}
	}

	@Nested
	@DisplayName("주관식 (SHORT_ANSWER)")
	class ShortAnswer {

		private final Problem problem = createProblem(
			ProblemType.SHORT_ANSWER, List.of(textAnswer("가비지 컬렉션")));

		@Test
		@DisplayName("정확히 일치하면 CORRECT를 반환한다")
		void correct() {
			assertThat(problem.grade(null, "가비지 컬렉션")).isEqualTo(AnswerType.CORRECT);
		}

		@Test
		@DisplayName("앞뒤 공백이 있어도 CORRECT를 반환한다")
		void correctWithWhitespace() {
			assertThat(problem.grade(null, "  가비지 컬렉션  ")).isEqualTo(AnswerType.CORRECT);
		}

		@Test
		@DisplayName("다른 텍스트를 입력하면 INCORRECT를 반환한다")
		void incorrect() {
			assertThat(problem.grade(null, "메모리 관리")).isEqualTo(AnswerType.INCORRECT);
		}

		@Test
		@DisplayName("답안이 null이면 예외가 발생한다")
		void nullAnswer() {
			assertThatThrownBy(() -> problem.grade(null, null))
				.isInstanceOf(CustomException.class);
		}
	}
}
