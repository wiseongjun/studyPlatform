package com.example.user.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "로그인 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

	@Schema(description = "로그인 ID", example = "kim_java")
	@NotBlank(message = "로그인 ID를 입력해 주세요.")
	private String loginId;

	@Schema(description = "비밀번호", example = "password123")
	@NotBlank(message = "비밀번호를 입력해 주세요.")
	private String password;
}
