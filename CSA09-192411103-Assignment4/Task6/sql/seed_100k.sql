-- Optional: 100,000 sample registrations for the "load all vs one page" comparison.
-- Needs MySQL 8 (recursive CTE). Run after schema.sql:  mysql -u root -p campus_events < seed_100k.sql

USE campus_events;
SET SESSION cte_max_recursion_depth = 100000;

INSERT INTO registrations (student_id, student_name, email, event_id)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 100000
)
SELECT LPAD(n, 9, '0'),
       CONCAT('Student ', n),
       CONCAT('student', n, '@example.edu'),
       1 + (n % 5)
FROM seq;

SELECT COUNT(*) AS total_registrations FROM registrations;
