package com.example.problem.dto.res;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.example.constants.AnswerType;

@Schema(description = "문제 제출 응답")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SubmitProblemResponse {

	@Schema(description = "문제 ID", example = "1")
	private Long problemId;

	@Schema(description = "정답 여부 (CORRECT / PARTIAL_CORRECT / INCORRECT)")
	private AnswerType answerStatus;

	@Schema(description = "문제 해설", example = "GC는 더 이상 참조되지 않는 객체를 자동으로 메모리에서 해제합니다.")
	private String explanation;

	@Schema(description = "정답 선택지 번호 목록 (객관식)", example = "[2]")
	private List<Integer> correctChoices;

	@Schema(description = "정답 텍스트 (주관식)", example = "가비지 컬렉션")
	private String correctTextAnswer;
}
