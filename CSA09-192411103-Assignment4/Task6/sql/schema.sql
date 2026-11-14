-- Task 6 - Campus Event Registration: schema (MySQL 8)
-- Run:  mysql -u root -p < schema.sql

CREATE DATABASE IF NOT EXISTS campus_events CHARACTER SET utf8mb4;
USE campus_events;

CREATE TABLE IF NOT EXISTS events (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(80) NOT NULL UNIQUE,
    event_date  DATE        NOT NULL
);

CREATE TABLE IF NOT EXISTS registrations (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id    CHAR(9)      NOT NULL,
    student_name  VARCHAR(60)  NOT NULL,
    email         VARCHAR(100) NOT NULL,
    event_id      INT          NOT NULL,
    registered_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reg_event FOREIGN KEY (event_id) REFERENCES events(id),
    -- The database itself refuses a second registration of the same student for the same
    -- event, so the rule still holds if two clients insert at the same moment.
    CONSTRAINT uq_student_event UNIQUE (student_id, event_id),
    INDEX idx_reg_event (event_id, id)
);

INSERT IGNORE INTO events (name, event_date) VALUES
    ('Cultural Night',     '2026-12-05'),
    ('Dance Competition',  '2026-12-06'),
    ('Music Fest',         '2026-12-07'),
    ('Drama Evening',      '2026-12-08'),
    ('Art Exhibition',     '2026-12-09');
