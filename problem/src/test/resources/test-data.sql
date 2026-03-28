INSERT INTO T_PROBLEM (id, chapter_id, title, content, explanation, type, is_delete, created_at)
VALUES (1, 1, 'Problem 1', 'Content 1', 'Explanation 1', 'SINGLE_CHOICE', false, '2026-01-01 00:00:00'),
       (2, 1, 'Problem 2', 'Content 2', 'Explanation 2', 'MULTI_CHOICE', false, '2026-01-01 00:00:00'),
       (3, 1, 'Deleted Problem', 'Content 3', 'Explanation 3', 'SINGLE_CHOICE', true, '2026-01-01 00:00:00'),
       (4, 2, 'Other Chapter', 'Content 4', 'Explanation 4', 'SHORT_ANSWER', false, '2026-01-01 00:00:00');

INSERT INTO T_PROBLEM_STATUS (id, problem_id, total_attempts, correct_attempts)
VALUES (1, 1, 29, 20),
       (2, 2, 30, 20);

INSERT INTO T_PROBLEM_CHOICE (id, problem_id, choice_number, choice_text)
VALUES (1, 1, 1, 'Choice A'),
       (2, 1, 2, 'Choice B'),
       (3, 1, 3, 'Choice C'),
       (4, 2, 1, 'Choice D'),
       (5, 2, 2, 'Choice E');
