# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Spring Cloud 마이크로서비스 기반 **학습/퀴즈 플랫폼** (`과제.md` 요구사항 구현). Java 21, Gradle 멀티모듈, Spring Boot 3.5.x, Spring Cloud BOM 2025.0.0.

**과제 핵심(요약):** 단원별 **랜덤 문제 조회**(미풀이·직전 스킵 제외), **문제 제출**(정답/부분/오답, 객관식·주관식), **풀이 이력 상세 조회**, 정답률(시도 30명 이상일 때만). 상세 스펙·우대사항은 루트 **`과제.md`** 참고.

## Build Commands

```bash
# 특정 모듈 빌드
./gradlew :chapter:build
./gradlew :problem:build

# Checkstyle (Naver 코딩 컨벤션)
./gradlew :chapter:checkstyleMain
./gradlew :problem:checkstyleMain

# Q클래스 재생성 (Entity 변경 후 필수)
./gradlew :problem:clean :problem:compileJava

# 서비스 로컬 실행 (Eureka 먼저 권장)
./gradlew :eureka:bootRun
./gradlew :gateway:bootRun
./gradlew :user:bootRun
./gradlew :problem:bootRun
./gradlew :chapter:bootRun
```

## Docker

설정·`.env`는 `docker/`. 서비스별 YAML은 `docker/yml/application-{service}.yml`.

```bash
cd docker && docker-compose -f docker-compose-db.yml up -d   # MySQL + Redis
docker-compose -f docker-compose-was.yml up -d               # 전체 서비스
```

## Module Structure & Ports

| 모듈 | 역할 | 기본 포트 |
|------|------|-----------|
| `eureka/` | 서비스 디스커버리 | 8761 |
| `gateway/` | API Gateway (WebFlux), Swagger UI 집계 | 8080 |
| `user/` | 사용자·풀이 시도 저장·풀이 목록/상세 API | 8081 |
| `problem/` | 문제·랜덤 출제·제출 채점·문제 상세(Feign) | 8082 |
| `chapter/` | 단원 API | 8083 |
| `flyway/` | DB 마이그레이션 전용 (1회 실행) | - |
| `common_core/` | 예외, ErrorCode, 공통 상수·Enum (`com.example.constants`) |
| `common_web/` | JPA, Redis, Swagger, QueryDSL 공통 설정 |
| `common_api/` | 서비스 간 Feign 인터페이스·DTO |
| `buildSrc/` | Gradle 커스텀 플러그인 (Groovy) |

## Gateway 라우팅 & Swagger

- **외부 API** (서비스별 prefix):
  - `/api/v1/problem/**` → `problem-service`
  - `/api/v1/user/**` → `user-service`
  - `/api/v1/chapter/**` → `chapter-service`
- **Internal** (Feign과 동일 경로; 게이트웨이에서 프록시 가능):
  - `/internal/v1/user/**` → `user-service`
  - `/internal/v1/problem/**` → `problem-service`  
  (`chapter-service`용 internal 게이트웨이 라우트 없음)
- **Discovery locator** (`lower-case-service-id: true`)도 활성화됨.
- **Swagger:** 게이트웨이 `springdoc.swagger-ui.urls`로 `/api-docs/problem`, `/api-docs/user`, `/api-docs/chapter` 통합.

## 과제 관련 대표 API (클라이언트는 보통 Gateway `8080` 기준)

**problem-service** (`ProblemController`, `/api/v1/problem`)

- `GET /random` — 미풀이 문제 랜덤 1개 (`RandomProblemFilter` 쿼리). 넘기기도 동일 API 재호출.
- `POST /{problemId}/submit` — 제출 (`SubmitProblemRequest`).
- `GET /list?chapterId=` — 챕터별 문제 목록.

**user-service** (`UserController`, `/api/v1/user`)

- `GET /{userId}/problem/solved/list` — 풀이 목록.
- `GET /{userId}/problem/solved/{problemId}` — 풀이 상세 (문항은 `ProblemFeignClient`로 조합).

## problem 모듈 책임 분리

- **`ProblemService`** — 오케스트레이션 (`@Transactional` 없음). Feign으로 풀이 ID 목록 조회, 랜덤 선택·응답 조립.
- **`ProblemWriteService`** — DB 쓰기·`ProblemStatus` 갱신 전담 (`@Transactional`).
- **`ProblemRepository` / `*QuerySupport`** — JPA·QueryDSL 조회·업데이트.
- **`ProblemCacheRepository`** — Redis. 챕터·유저별 **직전 스킵 problemId**, `problemIds` 캐시 등.
- **`ProblemSelector`** — 후보 ID 중 제외 목록 반영 랜덤 선택.
- **`ProblemHelper`** — 제외 ID 결합, 정답률(시도 수·부분정답 처리 규칙 등).
- **`ProblemMarker`** — 채점 순수 로직 (`AnswerType`).
- **`ProblemAsyncService`** — `UserFeignClient.saveAttempt` 비동기 호출 (DB 트랜잭션과 분리; 향후 메시징 전환 여지).

## Gradle Plugins (buildSrc)

각 비즈니스 모듈은 정확히 하나의 앱 플러그인 + 필요 시 `querydsl`:

- `base-library` — 공유 라이브러리 (JAR only)
- `spring-app` — 단독 Spring Boot 앱
- `spring-cloud-app` — Spring Cloud 마이크로서비스
- `querydsl` — QueryDSL 어노테이션 프로세서 (위와 함께 적용)

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

## Enum·상수 컨벤션 (`common_core` → `com.example.constants`)

사용자에게 노출되는 Enum은 `label` 필드와 `from(String label)` 역조회 메서드를 포함 (`AnswerType`, `ChapterCategory`, `ProblemType` 등).

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
Client → Gateway (8080)
           ├ /api/v1/problem|user|chapter/**  → 각 마이크로서비스
           └ /internal/v1/user|problem/**      → (Feign과 동일 경로, 프록시용)
                        ↑ Eureka(service-id) + MySQL + Redis

서비스 간: @FeignClient(name = "user-service" | "problem-service") → Eureka lb:// 직접 호출,
           경로는 /internal/v1/... (게이트웨이를 거치지 않아도 됨)
```

- Feign 인터페이스·DTO: `common_api`, `@EnableFeignClients(basePackages = "com.example.api")`
- `common_web` → 비즈니스 서비스가 의존 (JPA, Redis, Swagger, QueryDSL)
- `common_core` → `common_web`, `common_api`가 의존

## API 경로 컨벤션

- `/api/v1/**` — 외부 클라이언트용. 컨트롤러는 도메인별 하위 경로(`/api/v1/problem` 등) 사용.
- `/internal/v1/**` — 서비스 간 호출용 REST. Swagger에서 `@Hidden` 권장.

## Controller Swagger 컨벤션

```java
@Tag(name = "Domain", description = "도메인 관련 API")
@RestController
public class DomainController {

	@Operation(summary = "짧은 요약", description = "상세 설명")
	@GetMapping(...)
	public ResponseEntity<...> method(...) {
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

`domain/` 패키지에 순수 도메인 로직 분리. 인스턴스화 방지 (`private` 생성자 또는 Lombok `@NoArgsConstructor(access = PRIVATE)`).

```java
public final class ProblemMarker {
	private ProblemMarker() {
	}

	public static AnswerType mark(List<Integer> selectedChoices, String textAnswer, ProblemAnswerDto answer) {
		// ...
	}
}
```

## ProblemStatus 동시성 처리

`totalAttempts`, `correctAttempts` 업데이트는 dirty checking 대신 QueryDSL 단일 `update`로 원자적 갱신.
`SELECT → +1 → UPDATE` 패턴은 동시 요청 시 race condition 발생.

```java
queryFactory.update(qStatus)
	.set(qStatus.totalAttempts, qStatus.totalAttempts.add(1))
	.set(qStatus.correctAttempts,
		answerType == AnswerType.CORRECT ? qStatus.correctAttempts.add(1) : qStatus.correctAttempts)
	.where(qStatus.problem.id.eq(problemId))
	.execute();
```

## 트랜잭션·비동기 분리 컨벤션

외부 API(Feign) 호출은 DB 트랜잭션 밖에서 실행. DB write는 `{Domain}WriteService`에 `@Transactional`로 격리.

```
ProblemService           — 오케스트레이터, @Transactional 없음
ProblemWriteService      — DB write 전담, @Transactional
ProblemAsyncService      — saveAttempt Feign 등, 트랜잭션 밖·@Async
UserFeignClient 호출     — 랜덤 출제 시 solved ID 조회, 제출 후 시도 저장 등
```

## Key Versions

- Java: 21
- Spring Boot: 3.5.3 (`buildSrc/gradle.properties`)
- Spring Cloud BOM: 2025.0.0 (`gradle.properties`)
- QueryDSL: 5.1.0
- SpringDoc OpenAPI: 2.8.16 (서비스 MVC + 게이트웨이 WebFlux 각각)
