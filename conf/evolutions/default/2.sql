# Initial version

# --- !Ups

CREATE TABLE todos(
    uuid VARCHAR(36) PRIMARY KEY,
    text TEXT NOT NULL,
    checked BOOLEAN DEFAULT FALSE,
    belongsToUser VARCHAR(36) NOT NULL REFERENCES users(uuid),
    isDeleted BOOLEAN DEFAULT FALSE,
    createdOn timestamp DEFAULT now(),
    updatedOn timestamp DEFAULT NULL
);

CREATE TABLE comments(
    uuid VARCHAR(36) PRIMARY KEY,
    text TEXT NOT NULL,
    belongsToTodo VARCHAR(36) NOT NULL REFERENCES todos(uuid),
    belongsToUser VARCHAR(36) NOT NULL REFERENCES users(uuid),
    isDeleted BOOLEAN DEFAULT FALSE,
    createdOn timestamp DEFAULT now(),
    updatedOn timestamp DEFAULT NULL
)

# --- !Downs

DROP TABLE todos;
DROP TABLE comments;

-- Todos schema