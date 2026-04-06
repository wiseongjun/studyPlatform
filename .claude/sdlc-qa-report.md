# QA Report — Cycle 1

## Build Verification

| Check | Status | Details |
|-------|--------|---------|
| Compilation | PASS | 모든 모듈 (`buildSrc`, `common_core`, `common_api`, `common_web`, `user`, `problem`, `chapter`, `gateway`, `flyway`) 컴파일 성공 |
| Unit Tests | PASS | user, problem, chapter 전체 테스트 통과 (BUILD SUCCESSFUL) |
| Lint/Imports | PASS | 주요 파일 미사용 import 없음. Checkstyle warning 1건 (dev 결과 보고 기준, error 없음) |

---

## Code Review Summary

| Category | Rating | Critical | Warn | Nit | Praise |
|----------|--------|----------|------|-----|--------|
| Layered Architecture | PASS | 0 | 0 | 0 | 2 |
| Exception Handling | WARN | 0 | 2 | 0 | 1 |
| REST API Design | WARN | 0 | 1 | 1 | 1 |
| Naming & Consistency | PASS | 0 | 0 | 1 | 1 |
| Data Access | PASS | 0 | 0 | 0 | 2 |
| Security & Resilience | WARN | 0 | 3 | 0 | 1 |

---

## Detailed Findings

### [WARN] PassportAuthenticationFilter — finally 블록에서 SecurityContextHolder 조기 클리어

- **Location**: `common_web/src/main/java/com/example/config/PassportAuthenticationFilter.java:50`
- **Issue**: `finally` 블록에서 `SecurityContextHolder.clearContext()`를 호출하고 있음. 이는 `filterChain.doFilter()` 이후에도 실행되는데, Spring Security의 `SessionManagementFilter`나 `ExceptionTranslationFilter`가 응답을 처리하는 시점에 인증 컨텍스트가 이미 소거될 수 있음. `UserContext.clearUserPassport()`는 ThreadLocal 누수 방지를 위해 finally에 두는 것이 맞지만, `SecurityContextHolder.clearContext()`는 그 자리에 있으면 안 됨. Spring Security STATELESS 세션 정책 하에서는 Spring Security가 자체적으로 요청 후 SecurityContext를 정리하므로, 이 줄은 불필요하고 잠재적으로 위험함.
- **Impact**: 응답 쓰기 단계에서 인증 정보가 소거되어 `@PreAuthorize` 메서드 보안이 예상치 못하게 동작할 수 있음.
- **Fix**:
  ```java
  } finally {
      UserContext.clearUserPassport();
      // SecurityContextHolder.clearContext() 제거 — Spring Security가 STATELESS 정책으로 자동 처리
  }
  ```

### [WARN] @Async ProblemAsyncService — 예외 무음 처리 (기존 코드이나 Passport 도입 후 심화)

- **Location**: `problem/src/main/java/com/example/problem/service/ProblemAsyncService.java:17`
- **Issue**: `@Async` 메서드 `saveAttempt`에 try-catch 없음. Feign 호출 실패 시 예외가 비동기 스레드 내에서 조용히 소실됨. Passport 인프라 도입 이후 Feign 호출 시 `PassportRequestInterceptor`가 `UserContext`에서 Passport를 읽는데, `@Async`는 새 스레드에서 실행되므로 ThreadLocal이 전파되지 않아 Passport 헤더가 빠진 채 호출될 위험이 있음.
- **Impact**: `saveAttempt`가 조용히 실패하고, 내부 API가 인증 없이 노출(`/internal/v1/**` permitAll)되어 있어 당장 403은 아니지만, 향후 내부 API에도 인증을 적용한다면 `@Async` + ThreadLocal 전파 문제가 장애로 이어질 수 있음.
- **Fix**:
  ```java
  @Async
  public void saveAttempt(SaveAttemptRequest request) {
      try {
          userFeignClient.saveAttempt(request);
      } catch (Exception e) {
          log.error("비동기 시도 기록 저장 실패: {}", e.getMessage(), e);
          // 필요 시 재시도 큐에 적재
      }
  }
  ```

### [WARN] POST /api/v1/auth/login — 201 Created 미반환

- **Location**: `user/src/main/java/com/example/user/controller/AuthController.java:40`
- **Issue**: 로그인은 리소스 생성이 아닌 액션이지만, 현재 프로젝트 관례(Plan에서 `POST → 201 Created` 명시)와 달리 `ResponseEntity.ok()` (200 OK)를 반환함. 로그인 엔드포인트는 관용적으로 200이 허용되지만, 프로젝트 내 코드리뷰 기준과 계획에서 일치 여부를 확인해야 함.
- **Fix**: 로그인은 세션/토큰 자원 생성이므로 팀 합의에 따라 200 또는 201을 일관성 있게 유지. 현재 200도 기술적으로 틀리지 않으나, 프로젝트 컨벤션 문서와 명시적으로 맞출 것.

### [WARN] Gateway CORS — allowedOriginPatterns: "*" + allowCredentials: false

- **Location**: `gateway/src/main/resources/application.yaml:13-16`
- **Issue**: `allowedOriginPatterns: "*"`는 모든 Origin을 허용함. 현재 `allowCredentials: false`이므로 쿠키 기반 인증(access_token HttpOnly 쿠키)이 브라우저에서 동작하지 않음. JWT를 HttpOnly 쿠키로 전달하려면 반드시 `allowCredentials: true`와 구체적인 `allowedOriginPatterns`를 함께 설정해야 함.
- **Impact**: 브라우저에서 쿠키를 포함한 cross-origin 요청 시 `Set-Cookie`가 무시되어 로그인 후 JWT 쿠키가 저장되지 않음.
- **Fix**:
  ```yaml
  cors-configurations:
    '[/**]':
      allowedOriginPatterns: "http://localhost:3000,https://yourdomain.com"
      allowedMethods: "GET,POST,PUT,DELETE,OPTIONS"
      allowedHeaders: "*"
      allowCredentials: true
  ```

### [WARN] FeignClient — 타임아웃/재시도 미설정 (기존 코드)

- **Location**: `common_api/src/main/java/com/example/api/problem/client/ProblemFeignClient.java`, `common_api/src/main/java/com/example/api/user/client/UserFeignClient.java`
- **Issue**: FeignClient에 타임아웃 또는 재시도 설정 없음. Passport 인프라와 결합된 내부 서비스 호출이 증가한 현재, 하나의 서비스 지연이 전체 스레드 풀을 고갈시킬 수 있음.
- **Fix**: `application.yaml`에 Feign 타임아웃 추가:
  ```yaml
  spring:
    cloud:
      openfeign:
        client:
          config:
            default:
              connect-timeout: 3000
              read-timeout: 5000
  ```

### [NIT] AuthController — import 순서 비일관성

- **Location**: `user/src/main/java/com/example/user/controller/AuthController.java:7`
- **Issue**: `import org.springframework.http.HttpHeaders`가 `ResponseCookie` import 사이에 끼어 있음. Checkstyle(Naver 규칙)은 import 그룹 순서를 강제하므로, IDE formatter 실행 또는 `./gradlew checkstyleMain`으로 정렬 권장.

### [NIT] UserPassportDto — gateway 전용 DTO와 common_api DTO의 패키지 분리

- **Location**: `gateway/src/main/java/com/example/gateway/security/dto/UserPassportDto.java`
- **Issue**: Plan에서 "같은 필드명 유지 필수"를 명시했는데, gateway 전용 DTO와 common_api DTO 모두 `userId`, `loginId`, `role` 필드를 유지하고 있어 현재는 문제 없음. 단, 향후 필드 변경 시 두 클래스를 모두 동기화해야 함을 주석 또는 테스트로 명시하면 좋음.

---

### [PRAISE] JwtToPassportFilter — Reactive 패턴 모범 적용

- **Location**: `gateway/src/main/java/com/example/gateway/filter/JwtToPassportFilter.java:49-61`
- **What's good**: `Mono.fromCallable().subscribeOn(Schedulers.boundedElastic())`으로 블로킹 JWT 파싱을 논블로킹 파이프라인에서 격리하고, `.onErrorResume()`으로 안전한 에러 처리까지 구현. 노션 아키텍처 패턴을 정확히 구현.

### [PRAISE] PassportAuthenticationFilter — ThreadLocal 누수 방지

- **Location**: `common_web/src/main/java/com/example/config/PassportAuthenticationFilter.java:49`
- **What's good**: `finally { UserContext.clearUserPassport(); }`로 요청이 성공/실패 여부와 관계없이 ThreadLocal이 정리됨. 스레드 풀 재사용 환경에서 메모리 누수를 방지하는 올바른 패턴.

### [PRAISE] AuthService — 아이디/비밀번호 오류 메시지 통합

- **Location**: `user/src/main/java/com/example/user/service/AuthService.java:27-30`
- **What's good**: loginId 미존재와 비밀번호 불일치를 모두 `LOGIN_FAILED`로 동일하게 처리. 사용자 열거 공격(User Enumeration Attack) 방지를 위한 보안 모범 사례.

### [PRAISE] UserContext — 유틸리티 클래스 패턴

- **Location**: `common_api/src/main/java/com/example/api/passport/UserContext.java:9`
- **What's good**: private 생성자로 인스턴스화를 차단하고, 모든 메서드를 static으로 제공하는 전형적인 유틸리티 클래스 패턴. ThreadLocal 관리 메서드(get/set/clear)와 비즈니스 헬퍼(getCurrentUserId, isAdmin)를 명확히 구분하여 제공.

---

## Plan vs Reality

| Planned Change | Status | Note |
|----------------|--------|------|
| Flyway V2026.04.06.16.00.00 마이그레이션 | DONE | 계획과 동일하게 구현 |
| User Entity에 loginId, password, role 추가 | DONE | 계획과 동일 |
| UserRole enum (ROLE_USER, ROLE_ADMIN) | DONE | 계획과 동일 |
| UserRepository.findByLoginId() 추가 | DONE (변경) | 기존 QueryDSL UserRepository가 JPA 인터페이스 역할 불가 → `UserJpaRepository` 별도 생성. 합리적 개선. |
| JwtProperties, JwtTokenProvider (user) | DONE | 계획과 동일 |
| AuthService (BCrypt 검증 + JWT 생성) | DONE | `LoginResult` record 추가로 컨트롤러-서비스 분리 향상 |
| AuthController POST /api/v1/auth/login | DONE | HttpOnly 쿠키, SameSite=Lax 설정 포함 |
| LoginRequest, LoginResponse DTO | DONE | validation 포함 |
| user application.yaml jwt 설정 | DONE | 계획과 동일 |
| gateway UserPassportDto | DONE | 계획과 동일 |
| gateway JwtProperties | DONE | 계획과 동일 |
| gateway JwtTokenValidator | DONE | 계획과 동일 |
| gateway PassportSerializer | DONE | 계획과 동일 |
| gateway JwtToPassportFilter (order=-1) | DONE | 노션 패턴 그대로 적용 |
| gateway SecurityConfig (WebFlux) | DONE | 계획과 동일 |
| common_api UserPassportDto | DONE | 계획과 동일 |
| common_api PassportSerializer | DONE | static 유틸리티 구현 |
| common_api UserContext (ThreadLocal) | DONE | 계획과 동일 |
| common_api PassportRequestInterceptor | DONE | 계획과 동일 |
| common_web PassportAuthenticationFilter | DONE | SecurityContext 설정까지 추가 (계획 대비 개선) |
| common_web SecurityConfig | DONE | 계획과 동일 |
| (unplanned) PassportFeignConfig | ADDED | PassportRequestInterceptor Spring Bean 자동 등록. 필요한 추가. |
| (unplanned) common_web BOM 추가 | ADDED | spring-cloud BOM 추가. common_api 의존 시 필요. |
| (unplanned) user/problem/chapter 기존 테스트 수정 | ADDED | Security 도입 후 @WithMockUser, Passport 헤더 추가. 필요한 수정. |
| (unplanned) common_core ErrorCode.LOGIN_FAILED | ADDED | 계획에 없었으나 AuthService에서 필요. 올바른 추가. |
| (unplanned) @EnableConfigurationProperties 추가 | ADDED | user, gateway Application 클래스에 JwtProperties 등록. 필요한 수정. |
| gateway application.yaml CORS allowCredentials: false | DEVIATION | HttpOnly 쿠키 기반 인증에서 allowCredentials: true가 필요하나, false로 설정됨. WARN 수준 편차. |

---

## Verdict

### Overall: PASS

**PASS criteria 검증:**
- [x] Compilation 성공 — 전 모듈 BUILD SUCCESSFUL
- [x] 모든 테스트 통과 — user, problem, chapter 테스트 PASS
- [x] CRITICAL 발견 없음
- [x] 계획된 변경사항 누락 없음 (모든 계획 항목 DONE, 추가 변경은 모두 합리적)

---

**WARN 우선순위 수정 제안 (다음 사이클):**

1. **[WARN-1] Gateway CORS allowCredentials: false** — HttpOnly 쿠키 기반 JWT 인증이 브라우저에서 동작하려면 `allowCredentials: true` + 구체적 origin 설정 필수. 현재 상태로는 실제 브라우저 클라이언트가 로그인 쿠키를 받지 못함.

2. **[WARN-2] PassportAuthenticationFilter finally의 SecurityContextHolder.clearContext()** — `filterChain.doFilter()` 이후 응답 처리 중 인증 컨텍스트를 조기 소거. Spring Security STATELESS 설정에서는 불필요하고 잠재적으로 위험한 코드 제거 권장.

3. **[WARN-3] @Async ProblemAsyncService — ThreadLocal 전파 누락 + 예외 무음** — Passport 도입 이후 `@Async` 스레드에서 UserContext ThreadLocal이 비어 있음. 현재는 내부 API가 permittAll이므로 장애로 이어지지 않지만, 향후 내부 인증 강화 시 데이터 일관성 문제가 될 수 있음.

4. **[WARN-4] FeignClient 타임아웃 미설정** — 서비스 간 호출 증가(Passport 전파 포함) 이후 타임아웃 없는 Feign 호출은 Thread starvation 위험. `application.yaml`에 default connectTimeout/readTimeout 추가 권장.