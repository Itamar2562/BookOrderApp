CREATE TABLE IF NOT EXISTS Users
(
    id UUID PRIMARY KEY,
    manager_id UUID REFERENCES Users (id) ON DELETE SET NULL,
    first_name VARCHAR(32) NOT NULL,
    last_name VARCHAR(32) NOT NULL,
    email VARCHAR (64) NOT NULL UNIQUE,
    password VARCHAR(64) NOT NULL,
    user_role VARCHAR(32) NOT NULL
    );


CREATE TABLE IF NOT EXISTS Orders (
                                      id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                                      created_at TIMESTAMP NOT NULL,
                                      finished_at TIMESTAMP,
                                      status VARCHAR(32),
    manager_id UUID REFERENCES Users (id) ON DELETE CASCADE
    );


CREATE TABLE IF NOT EXISTS Books (
                                     id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                                     title VARCHAR(256) NOT NULL,
    author VARCHAR(256) NOT NULL,
    amount INT,
    created_at TIMESTAMP NOT NULL,

    worker_id UUID REFERENCES Users (id) ON DELETE CASCADE,
    order_id INT REFERENCES Orders (id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS Tokens (
                                      id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                                      token VARCHAR(256) NOT NULL,
    token_type VARCHAR(32) NOT NULL,
    revoked BOOLEAN NOT NULL,
    expired BOOLEAN NOT NULL,
    user_id UUID REFERENCES Users (id) ON DELETE CASCADE
    );