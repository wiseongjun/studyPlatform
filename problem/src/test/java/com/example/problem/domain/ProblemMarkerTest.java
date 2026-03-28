package com.example.problem.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.example.constants.AnswerType;
import com.example.constants.ProblemType;
import com.example.exception.CustomException;
import com.example.problem.dto.internal.ProblemAnswerDto;

class ProblemMarkerTest {

	@Nested
	@DisplayName("단일 정답 (SINGLE_CHOICE)")
	class SingleChoice {

		private final ProblemAnswerDto answer = new ProblemAnswerDto(1L, ProblemType.SINGLE_CHOICE, List.of(2), null, "해설");

		@Test
		@DisplayName("정답 선택지를 고르면 CORRECT를 반환한다")
		void correct() {
			assertThat(ProblemMarker.mark(List.of(2), null, answer)).isEqualTo(AnswerType.CORRECT);
		}

		@Test
		@DisplayName("오답 선택지를 고르면 INCORRECT를 반환한다")
		void incorrect() {
			assertThat(ProblemMarker.mark(List.of(1), null, answer)).isEqualTo(AnswerType.INCORRECT);
		}

		@Test
		@DisplayName("선택지가 비어있으면 예외가 발생한다")
		void emptySelected() {
			assertThatThrownBy(() -> ProblemMarker.mark(List.of(), null, answer))
				.isInstanceOf(CustomException.class);
		}

		@Test
		@DisplayName("선택지가 null이면 예외가 발생한다")
		void nullSelected() {
			assertThatThrownBy(() -> ProblemMarker.mark(null, null, answer))
				.isInstanceOf(CustomException.class);
		}
	}

	@Nested
	@DisplayName("복수 정답 (MULTI_CHOICE)")
	class MultiChoice {

		private final ProblemAnswerDto answer = new ProblemAnswerDto(1L, ProblemType.MULTI_CHOICE, List.of(1, 2), null, "해설");

		@Test
		@DisplayName("정답 선택지를 모두 고르면 CORRECT를 반환한다")
		void allCorrect() {
			assertThat(ProblemMarker.mark(List.of(1, 2), null, answer)).isEqualTo(AnswerType.CORRECT);
		}

		@Test
		@DisplayName("정답 일부와 오답을 함께 선택하면 PARTIAL_CORRECT를 반환한다")
		void partialWithWrong() {
			assertThat(ProblemMarker.mark(List.of(1, 3), null, answer)).isEqualTo(AnswerType.PARTIAL_CORRECT);
		}

		@Test
		@DisplayName("정답을 모두 포함하더라도 오답이 있으면 PARTIAL_CORRECT를 반환한다")
		void allCorrectPlusWrong() {
			assertThat(ProblemMarker.mark(List.of(1, 2, 3), null, answer)).isEqualTo(AnswerType.PARTIAL_CORRECT);
		}

		@Test
		@DisplayName("정답을 하나도 선택하지 않으면 INCORRECT를 반환한다")
		void noneCorrect() {
			assertThat(ProblemMarker.mark(List.of(3, 4), null, answer)).isEqualTo(AnswerType.INCORRECT);
		}

		@Test
		@DisplayName("선택지가 null이면 예외가 발생한다")
		void nullSelected() {
			assertThatThrownBy(() -> ProblemMarker.mark(null, null, answer))
				.isInstanceOf(CustomException.class);
		}
	}

	@Nested
	@DisplayName("주관식 (SHORT_ANSWER)")
	class ShortAnswer {

		private final ProblemAnswerDto answer = new ProblemAnswerDto(1L, ProblemType.SHORT_ANSWER, null, "가비지 컬렉션", "해설");

		@Test
		@DisplayName("정확히 일치하면 CORRECT를 반환한다")
		void correct() {
			assertThat(ProblemMarker.mark(null, "가비지 컬렉션", answer)).isEqualTo(AnswerType.CORRECT);
		}

		@Test
		@DisplayName("앞뒤 공백이 있어도 CORRECT를 반환한다")
		void correctWithWhitespace() {
			assertThat(ProblemMarker.mark(null, "  가비지 컬렉션  ", answer)).isEqualTo(AnswerType.CORRECT);
		}

		@Test
		@DisplayName("다른 텍스트를 입력하면 INCORRECT를 반환한다")
		void incorrect() {
			assertThat(ProblemMarker.mark(null, "메모리 관리", answer)).isEqualTo(AnswerType.INCORRECT);
		}

		@Test
		@DisplayName("답안이 null이면 예외가 발생한다")
		void nullAnswer() {
			assertThatThrownBy(() -> ProblemMarker.mark(null, null, answer))
				.isInstanceOf(CustomException.class);
		}
	}
}
