-- T_USER
INSERT INTO T_USER (name)
VALUES ('김자바'),
       ('이스프링'),
       ('박알고');

-- T_CHAPTER
INSERT INTO T_CHAPTER (name, category)
VALUES ('자바 기초', 'JAVA'),
       ('스프링 핵심', 'SPRING'),
       ('데이터베이스 기초', 'DATABASE'),
       ('알고리즘 기초', 'ALGORITHM'),
       ('자료구조 기초', 'DATA_STRUCTURE');

-- T_PROBLEM
-- JAVA 챕터 (chapter_id=1): SINGLE_CHOICE 2개, SHORT_ANSWER 1개
INSERT INTO T_PROBLEM (chapter_id, title, content, explanation, type)
VALUES (1, 'Java에서 기본 자료형이 아닌 것은?',
        '다음 중 Java의 기본 자료형(Primitive Type)이 아닌 것을 고르시오.',
        'String은 클래스(참조 타입)이며 기본 자료형이 아닙니다. 기본 자료형은 int, char, boolean, double 등이 있습니다.',
        'SINGLE_CHOICE'),
       (1, 'Java에서 오버라이딩 조건으로 옳은 것은?',
        '다음 중 메서드 오버라이딩(Overriding)의 조건으로 옳은 것을 고르시오.',
        '오버라이딩은 상위 클래스의 메서드와 동일한 시그니처(이름, 매개변수)를 가져야 하며, 접근 제어자는 같거나 더 넓어야 합니다.',
        'SINGLE_CHOICE'),
       (1, 'Java에서 인터페이스와 추상 클래스의 차이점을 서술하시오.',
        '인터페이스(interface)와 추상 클래스(abstract class)의 차이점을 서술하시오.',
        '인터페이스는 다중 구현이 가능하고 기본적으로 모든 메서드가 추상 메서드입니다. 추상 클래스는 단일 상속만 가능하며 일반 메서드와 추상 메서드를 함께 가질 수 있습니다.',
        'SHORT_ANSWER'),
-- SPRING 챕터 (chapter_id=2): SINGLE_CHOICE 1개, MULTI_CHOICE 1개
       (2, 'Spring Bean의 기본 스코프는?',
        'Spring에서 Bean을 등록할 때 별도 설정이 없을 경우 기본 스코프(Scope)는 무엇인가?',
        'Spring Bean의 기본 스코프는 Singleton입니다. 컨테이너당 하나의 인스턴스만 생성됩니다.',
        'SINGLE_CHOICE'),
       (2, 'Spring에서 의존성 주입 방식으로 옳은 것을 모두 고르시오.',
        '다음 중 Spring에서 지원하는 의존성 주입(DI) 방식을 모두 고르시오.',
        '생성자 주입, 세터 주입, 필드 주입 세 가지 방식이 있습니다. 생성자 주입이 권장됩니다.',
        'MULTI_CHOICE'),
-- DATABASE 챕터 (chapter_id=3): SINGLE_CHOICE 1개
       (3, 'SQL에서 트랜잭션의 특성이 아닌 것은?',
        '다음 중 트랜잭션의 ACID 특성에 해당하지 않는 것을 고르시오.',
        'ACID는 Atomicity(원자성), Consistency(일관성), Isolation(격리성), Durability(지속성)입니다. Flexibility는 해당하지 않습니다.',
        'SINGLE_CHOICE'),
-- ALGORITHM 챕터 (chapter_id=4): SHORT_ANSWER 1개
       (4, '버블 정렬의 시간 복잡도를 서술하시오.',
        '버블 정렬(Bubble Sort)의 최선, 평균, 최악의 시간 복잡도를 Big-O 표기법으로 서술하시오.',
        '최선 O(n), 평균 O(n²), 최악 O(n²)입니다. 최선의 경우는 이미 정렬된 상태일 때 최적화된 버블 정렬 기준입니다.',
        'SHORT_ANSWER');

-- T_PROBLEM_CHOICE (객관식 문제: id=1,2,4,5,6)
-- 문제1: Java 기본 자료형이 아닌 것
INSERT INTO T_PROBLEM_CHOICE (problem_id, choice_number, choice_text)
VALUES (1, 1, 'int'),
       (1, 2, 'char'),
       (1, 3, 'String'),
       (1, 4, 'boolean');

-- 문제2: 오버라이딩 조건
INSERT INTO T_PROBLEM_CHOICE (problem_id, choice_number, choice_text)
VALUES (2, 1, '메서드 이름이 달라도 된다'),
       (2, 2, '매개변수 타입이 달라도 된다'),
       (2, 3, '접근 제어자는 같거나 더 넓어야 한다'),
       (2, 4, '반환 타입은 반드시 달라야 한다');

-- 문제4: Spring Bean 기본 스코프
INSERT INTO T_PROBLEM_CHOICE (problem_id, choice_number, choice_text)
VALUES (4, 1, 'Prototype'),
       (4, 2, 'Singleton'),
       (4, 3, 'Request'),
       (4, 4, 'Session');

-- 문제5: Spring DI 방식 (복수 정답)
INSERT INTO T_PROBLEM_CHOICE (problem_id, choice_number, choice_text)
VALUES (5, 1, '생성자 주입'),
       (5, 2, '세터 주입'),
       (5, 3, '필드 주입'),
       (5, 4, '정적 주입');

-- 문제6: ACID 특성이 아닌 것
INSERT INTO T_PROBLEM_CHOICE (problem_id, choice_number, choice_text)
VALUES (6, 1, 'Atomicity'),
       (6, 2, 'Consistency'),
       (6, 3, 'Flexibility'),
       (6, 4, 'Durability');

-- T_PROBLEM_ANSWER
INSERT INTO T_PROBLEM_ANSWER (problem_id, answer_number, answer_text)
VALUES (1, 3, NULL),
       (2, 3, NULL),
       (3, NULL, '인터페이스는 다중 구현이 가능하고 추상 메서드만 선언 가능하다. 추상 클래스는 단일 상속만 가능하며 일반 메서드와 추상 메서드를 함께 가질 수 있다.'),
       (4, 2, NULL),
       (5, 1, NULL),
       (5, 2, NULL),
       (5, 3, NULL),
       (6, 3, NULL),
       (7, NULL, '최선 O(n), 평균 O(n²), 최악 O(n²)');

-- T_PROBLEM_STATUS (모든 문제 초기값 0)
INSERT INTO T_PROBLEM_STATUS (problem_id, total_attempts, correct_attempts)
VALUES (1, 0, 0),
       (2, 0, 0),
       (3, 0, 0),
       (4, 0, 0),
       (5, 0, 0),
       (6, 0, 0),
       (7, 0, 0);

-- T_USER_PROBLEM_ATTEMPT (유저들의 풀이 기록)
INSERT INTO T_USER_PROBLEM_ATTEMPT (user_id, problem_id, chapter_id, answer_status)
VALUES (1, 1, 1, 'CORRECT'),
       (1, 2, 1, 'INCORRECT'),
       (2, 4, 2, 'CORRECT'),
       (2, 5, 2, 'PARTIAL_CORRECT'),
       (3, 6, 3, 'CORRECT');

-- T_USER_PROBLEM_ANSWER (위 attempt에 대응하는 제출 답안)
INSERT INTO T_USER_PROBLEM_ANSWER (attempt_id, choice_number, answer_text)
VALUES (1, 3, NULL),
       (2, 1, NULL),
       (3, 2, NULL),
       (4, 1, NULL),
       (5, 3, NULL);
