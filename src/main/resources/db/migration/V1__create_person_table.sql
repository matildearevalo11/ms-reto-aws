CREATE TABLE person (
    identification VARCHAR(30) PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(254) NOT NULL UNIQUE
);

CREATE INDEX idx_person_email_lower ON person (LOWER(email));
