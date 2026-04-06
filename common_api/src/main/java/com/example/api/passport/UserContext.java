package com.example.api.passport;

import com.example.api.passport.dto.UserPassportDto;

public class UserContext {

	private static final ThreadLocal<UserPassportDto> CONTEXT = new ThreadLocal<>();

	private UserContext() {
	}

	public static UserPassportDto getUserPassport() {
		return CONTEXT.get();
	}

	public static void setUserPassport(UserPassportDto passport) {
		CONTEXT.set(passport);
	}

	public static void clearUserPassport() {
		CONTEXT.remove();
	}

	public static Long getCurrentUserId() {
		UserPassportDto passport = CONTEXT.get();
		return passport != null ? passport.getUserId() : null;
	}

	public static String getCurrentUserLoginId() {
		UserPassportDto passport = CONTEXT.get();
		return passport != null ? passport.getLoginId() : null;
	}

	public static boolean isAdmin() {
		UserPassportDto passport = CONTEXT.get();
		return passport != null && "ROLE_ADMIN".equals(passport.getRole());
	}
}
