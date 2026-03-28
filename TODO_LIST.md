#TODO-LIST

0. Flyway, testController 가짜 데이터 삭제


1. 챕터 API 생성 (단원 리스트 조회)

- 인터페이스 DTO 생성
- Validation 처리

2. 문제 API 생성

- Req, Res, internal DTO 생성
- 랜덤 문제 조회 (사용자 푼문제 + 직전 문제 - Client)
- 단원 문제 리스트 조회 (단원ID)
- 문제 제출 (Status 업데이트 -> 사용자 문제 저장 - Client)
    - 정합성을 완벽히 처리하려면 Kafka 필요

3. 사용자 API 생성

- 사용자 문제 저장 (정답Type, Answer 받기)
- 푼 문제 조회 (문제정보 - Client + 사용자 푼문제정보)

4. 요구사항에 맞는 Entity 생성

- Chapter
- Problem - delete 컬럼으로 관리 - 수정 시 기존 삭제처리(User 시도 정보때문에) 새로 만들기
- ProblemStatus - 정답률, 시도 횟수 등 집계 데이터
- ProblemChoice - 객관식 지문
- ProblemAnswer - Problem Type에 맞는 정답
- User
- UserProblemAttempt - 사용자가 시도 정보 리스트(문제 + 정답 오답 포함)
- USerProblemAnswer - 사용자가 제출했던 정답 정보

5. 챕터, 문제, 사용자 DB 더미데이터

- 챕터, 문제, 사용자 등록수정삭제 넣기에는 시간 부족
- FLYWAY로 간단하게 적용

6. 랜덤 문제 조회 기능, 테스트 구현 - Problem
7. 문제 제출 기능, 테스트 구현 - Problem
8. 푼 문제 상세 조회 기능, 테스트 구현 - User
9. 요구사항 확인

- lastSkippedIndex 처리
- 캐싱, 인덱스 등 성능 고려 처리
- DDD, 객체지향 확인

999. Security-User 연동 / docker Swarm 등 서버 분리 처리 / Kafka 등 정합성 처리
