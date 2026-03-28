# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Spring Cloud microservices platform for a programming study/quiz service. Built with Java 21, Gradle multi-module, and
Spring Cloud 2025.0.0.

## Build Commands

```bash
# Build a specific module
./gradlew :chapter:build

# Run checkstyle (Naver coding standards)
./gradlew :chapter:checkstyleMain

# Q클래스 재생성 (Entity 변경 후 필수)
./gradlew :chapter:clean :chapter:compileJava

# 서비스 로컬 실행
./gradlew :eureka:bootRun
./gradlew :gateway:bootRun
./gradlew :chapter:bootRun
```

## Docker

Config files and `.env` are under `docker/`. Service YAMLs are in `docker/yml/application-{service}.yml`.

```bash
cd docker && docker-compose -f docker-compose-db.yml up -d   # MySQL + Redis
docker-compose -f docker-compose-was.yml up -d               # 전체 서비스
```

## Module Structure

```
eureka/          - 서비스 디스커버리 (port 8761)
gateway/         - API 게이트웨이 (port 8080, /api/v1/** 라우팅)
chapter/         - 단원 서비스 (port 8083)
problem/         - 문제 서비스
user/            - 사용자 서비스
flyway/          - DB 마이그레이션 전용 (1회 실행)
common_core/     - 예외, ErrorCode, 공통 Enum (type 패키지)
common_web/      - JPA, Redis, Swagger, QueryDSL 설정
common_api/      - 서비스 간 Feign 클라이언트 인터페이스
buildSrc/        - Gradle 커스텀 플러그인 (Groovy)
```

## Gradle Plugins (buildSrc)

각 모듈은 정확히 하나를 적용:

- `base-library` — 공유 라이브러리 (JAR only)
- `spring-app` — 단독 Spring Boot 앱
- `spring-cloud-app` — Spring Cloud 마이크로서비스
- `querydsl` — QueryDSL 어노테이션 프로세서 (위 플러그인과 함께 적용)

## DTO 레이어 컨벤션

각 서비스의 `dto/` 패키지를 3개로 분리:

```
dto/
  req/       - 외부 요청 수신. Filter 클래스, @Valid validation, @Schema Swagger 선언.
               toCondition() 메서드로 internal Condition 변환 책임.
  res/       - 외부 응답 전용. 로직 없음. Jackson 역직렬화를 위해 @NoArgsConstructor 필수.
  internal/  - 서비스 내부 전용.
               postfix: Condition (DB 조회 조건), Command (DB 쓰기), Dto (레이어 간 변환)
```

**흐름:** `Controller → Service(Filter) → Service calls filter.toCondition() → Repository(Condition)`

## QuerySupport 패턴

Repository의 QueryDSL 조건 메서드는 `repository/support/{Domain}QuerySupport.java`로 분리.

```java
// private 생성자로 인스턴스화 방지
public final class ChapterQuerySupport {
	private ChapterQuerySupport() {
	}

	public static BooleanExpression nameContains(String name) {
		return name != null ? QChapter.chapter.name.containsIgnoreCase(name) : null;
	}
}
// Repository에서: ChapterQuerySupport.nameContains(condition.getName())
```

null을 반환하면 QueryDSL `.where()`가 해당 조건을 자동으로 무시함.

## Enum 컨벤션 (common_core/type 패키지)

사용자에게 노출되는 Enum은 `label` 필드와 `from(String label)` 역조회 메서드를 포함:

```java
public enum ChapterCategory {
	JAVA("자바"), SPRING("스프링");

	private final String label;

	ChapterCategory(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public static ChapterCategory from(String label) {
		for (ChapterCategory c : values()) {
			if (c.label.equals(label))
				return c;
		}
		throw new CustomException(ErrorCode.INVALID_INPUT);
	}
}
```

## Naver Checkstyle — Import 그룹 순서

그룹 간 빈 줄 1개, 그룹 내 알파벳 순 정렬, 그룹 내 빈 줄 없음:

```
java.*

org.*

com.* (서드파티: com.querydsl, com.fasterxml 등)

io.*, jakarta.*, lombok.*  ← catch-all, 알파벳 순(io < jakarta < lombok), 그룹 내 blank 없음

com.example.*
```

## Architecture

```
Client → Gateway(/api/v1/**) → [chapter | problem | user service]
                                    ↑ discovered via Eureka
                               MySQL (3306) + Redis (6379)

서비스 간 Feign 통신 → Gateway(/internal/v1/**)
```

- Feign 클라이언트는 `common_api`에 정의, `@EnableFeignClients(basePackages = "com.example.api")`로 활성화
- `common_web` → 모든 비즈니스 서비스가 의존 (JPA, Redis, Swagger, QueryDSL 제공)
- `common_core` → `common_web`, `common_api`가 의존

## API 경로 컨벤션

- `/api/v1/**` — 외부 클라이언트용. Gateway에서 각 서비스로 라우팅.
- `/internal/v1/**` — 서비스 간 Feign 전용. Swagger에서 `@Hidden`으로 숨김.

## Controller Swagger 컨벤션

```java

@Tag(name = "Domain", description = "도메인 관련 API")
@RestController
public class DomainController {

	@Operation(summary = "짧은 요약", description = "상세 설명")
	@GetMapping(...)
	public ResponseEntity<...>

	method(...) {
	}
}

// 내부 전용 컨트롤러
@Hidden
@RestController
@RequestMapping("/internal/v1/...")
public class InternalDomainController {
}
```

- req DTO 필드: `@Schema(description = "...", example = "...")`
- res DTO 클래스: `@Schema(description = "... 응답")`, 필드마다 `@Schema` 추가

## 캐싱 컨벤션

- `@Cacheable` / `@CacheEvict`는 Repository 메서드에 선언
- TTL 기본값: 30분 (common_web RedisConfig)
- 캐시 무효화가 필요한 경우 주석으로 `@CacheEvict` 위치 명시

```java
// 문제 추가/삭제 시: @CacheEvict(value = "problemIds", key = "#chapterId") 로 무효화 필요
@Cacheable(value = "problemIds", key = "#chapterId")
public List<Long> getProblemIds(Long chapterId) { ...}
```

## Redis 직접 의존 분리 컨벤션

Service에서 `RedisTemplate` 직접 주입 금지. `{Domain}CacheRepository`로 래핑 후 주입.

```
problem/repository/ProblemCacheRepository  ← RedisTemplate 직접 사용
problem/service/ProblemService             ← ProblemCacheRepository 주입
```

## 도메인 클래스 컨벤션

`domain/` 패키지에 순수 도메인 로직 분리. 인스턴스화 방지 (`private` 생성자).

```java
public class ProblemMarker {
	private ProblemMarker() {
	}

	public static AnswerType mark(SubmitProblemRequest request, ProblemAnswerDto answer) { ...}
}
```

## ProblemStatus 동시성 처리

`totalAttempts`, `correctAttempts` 업데이트는 dirty checking 대신 QueryDSL atomic update 사용.
`SELECT → +1 → UPDATE` 패턴은 동시 요청 시 race condition 발생.

```java
queryFactory.update(qStatus)
    .

set(qStatus.totalAttempts, qStatus.totalAttempts.add(1))
	.

set(qStatus.correctAttempts,
	answerType ==AnswerType.CORRECT?qStatus.correctAttempts.add(1) :qStatus.correctAttempts)
	.

where(qStatus.problem.id.eq(problemId))
	.

execute();
```

## 트랜잭션 분리 컨벤션

외부 API(Feign) 호출은 DB 트랜잭션 밖에서 실행.
write 로직은 별도 `{Domain}WriteService`로 분리하여 `@Transactional` 격리.

```
ProblemService          — 오케스트레이터, @Transactional 없음
ProblemWriteService     — DB write 전담, @Transactional
userFeignClient 호출    — ProblemWriteService 완료 후 실행
```

## Key Versions

- Java: 21
- Spring Cloud BOM: 2025.0.0
- QueryDSL: 5.1.0
- SpringDoc OpenAPI: 2.8.16
