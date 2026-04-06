package com.example.user.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "로그인 응답")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

	@Schema(description = "사용자 ID", example = "1")
	private Long userId;

	@Schema(description = "로그인 ID", example = "kim_java")
	private String loginId;

	@Schema(description = "역할", example = "ROLE_USER")
	private String role;
}
