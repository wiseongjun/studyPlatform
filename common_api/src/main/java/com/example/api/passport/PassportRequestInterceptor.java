package com.example.api.passport;

import feign.RequestInterceptor;
import feign.RequestTemplate;

import com.example.api.passport.dto.UserPassportDto;

public class PassportRequestInterceptor implements RequestInterceptor {

	private static final String PASSPORT_HEADER = "X-User-Passport";

	@Override
	public void apply(RequestTemplate template) {
		UserPassportDto passport = UserContext.getUserPassport();
		if (passport != null) {
			template.header(PASSPORT_HEADER, PassportSerializer.serialize(passport));
		}
	}
}
