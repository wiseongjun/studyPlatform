package com.example.gateway.security.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserPassportDto {

	private Long userId;
	private String loginId;
	private String role;
}
