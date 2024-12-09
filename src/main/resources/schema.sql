CREATE TABLE users (
    id IDENTITY,
    userName VARCHAR NOT NULL
  );

CREATE TABLE cards (
    id IDENTITY,
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
    coin INTEGER NOT NULL,
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
    bet INTEGER,
    isActive BOOLEAN
);

CREATE TABLE room (
    id IDENTITY,
    roomName VARCHAR,
    user1id INTEGER,
    user1Status BOOLEAN,
    user2id INTEGER,
    user2Status BOOLEAN
);

CREATE TABLE role (
  id IDENTITY,
  roleName VARCHAR NOT NULL
);
