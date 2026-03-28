package com.example.user.dto.res;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.example.constants.AnswerType;
import com.example.constants.ProblemType;

@Schema(description = "풀었던 문제 상세 조회 응답")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AttemptDetailResponse {

	@Schema(description = "문제 ID", example = "1")
	private Long problemId;

	@Schema(description = "문제 제목", example = "GC의 역할")
	private String title;

	@Schema(description = "문제 내용", example = "Java에서 GC의 역할은 무엇인가요?")
	private String content;

	@Schema(description = "문제 유형")
	private ProblemType problemType;

	@Schema(description = "정답 여부 (CORRECT / PARTIAL_CORRECT / INCORRECT)")
	private AnswerType answerType;

	@Schema(description = "문제 해설", example = "GC는 더 이상 참조되지 않는 객체를 자동으로 메모리에서 해제합니다.")
	private String explanation;

	@Schema(description = "정답 선택지 번호 목록 (객관식)", example = "[2]")
	private List<Integer> correctChoices;

	@Schema(description = "정답 텍스트 (주관식)", example = "가비지 컬렉션")
	private String correctTextAnswer;

	@Schema(description = "사용자가 선택한 선택지 번호 목록", example = "[1, 2]")
	private List<Integer> userChoices;

	@Schema(description = "사용자가 입력한 텍스트 답안", example = "GC")
	private String userTextAnswer;

	@Schema(description = "정답률 (30명 미만 풀었을 경우 null)", example = "67")
	private Integer answerCorrectRate;
}
