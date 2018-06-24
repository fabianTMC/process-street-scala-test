# Initial version

# --- !Ups

CREATE TABLE users(
    uuid VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password TEXT NOT NULL,
    salt TEXT NOT NULL,
    alg VARCHAR(10) NOT NULL,
    verificationHash TEXT NOT NULL,
    verified BOOL DEFAULT FALSE,
    createdOn timestamp DEFAULT now(),
    updatedOn timestamp DEFAULT NULL,
    verifiedOn timestamp DEFAULT NULL
);

# --- !Downs

DROP TABLE users;

-- User schema