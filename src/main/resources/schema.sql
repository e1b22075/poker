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
    isActive BOOLEAN
);
