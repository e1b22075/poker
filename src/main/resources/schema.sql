CREATE TABLE users ( id IDENTITY,
userName VARCHAR NOT NULL,
password_hash VARCHAR(255) NOT NULL,
email VARCHAR(100) NOT NULL,
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP);

CREATE TABLE cards (
    id IDENTITY PRIMARY KEY,
    rid INTEGER NOT NULL,
    num INTEGER NOT NULL,
    cardtype VARCHAR NOT NULL,
    isActive BOOLEAN
);

CREATE TABLE hand (
    id IDENTITY,
    userid INTEGER NOT NULL,
    hand1id INTEGER NOT NULL,
    hand2id INTEGER NOT NULL,
    hand3id INTEGER NOT NULL,
    hand4id INTEGER NOT NULL,
    hand5id INTEGER NOT NULL,
    turn INTEGER NOT NULL,
    roleid INTEGER,
    rolenum INTEGER,
    isActive BOOLEAN
);


CREATE TABLE match (
    id IDENTITY,
    user1id INTEGER,
    user2id INTEGER,
    user1coin INTEGER,
    user2coin INTEGER,
    user1state VARCHAR,
    user2state VARCHAR,
    user1hand INTEGER,
    user2hand INTEGER,
    bet INTEGER,
    round INTEGER,
    rid INTEGER,
    isActive BOOLEAN
);

CREATE TABLE room (
    id IDENTITY,
    roomName VARCHAR,
    user1id INTEGER,
    user1Name VARCHAR,
    user1Status BOOLEAN,
    user2id INTEGER,
    user2Name VARCHAR,
    user2Status BOOLEAN
);

CREATE TABLE role (
  id IDENTITY,
  roleName VARCHAR NOT NULL
);
