package com.example.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.RequiredArgsConstructor;

import com.example.gateway.security.JwtTokenValidator;
import com.example.gateway.security.PassportSerializer;
import com.example.gateway.security.dto.UserPassportDto;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@RequiredArgsConstructor
public class JwtToPassportFilter implements GlobalFilter, Ordered {

	private static final String PASSPORT_HEADER = "X-User-Passport";
	private static final String ACCESS_TOKEN_NAME = "access_token";
	private static final String AUTH_PATH_PREFIX = "/api/v1/auth/";

	private final JwtTokenValidator jwtTokenValidator;
	private final PassportSerializer passportSerializer;

	@Override
	public int getOrder() {
		return -1;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		String path = exchange.getRequest().getURI().getPath();
		if (path.startsWith(AUTH_PATH_PREFIX)) {
			return chain.filter(exchange);
		}

		String token = extractToken(exchange);
		if (token == null || !jwtTokenValidator.validateToken(token)) {
			exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
			return exchange.getResponse().setComplete();
		}

		return Mono.fromCallable(() -> jwtTokenValidator.parsePayload(token))
			.subscribeOn(Schedulers.boundedElastic())
			.flatMap(passport -> {
				String serialized = passportSerializer.serialize(passport);
				ServerWebExchange mutated = exchange.mutate()
					.request(req -> req.header(PASSPORT_HEADER, serialized))
					.build();
				return chain.filter(mutated);
			})
			.onErrorResume(e -> {
				exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
				return exchange.getResponse().setComplete();
			});
	}

	private String extractToken(ServerWebExchange exchange) {
		HttpCookie cookie = exchange.getRequest().getCookies().getFirst(ACCESS_TOKEN_NAME);
		return cookie != null ? cookie.getValue() : null;
	}
}
