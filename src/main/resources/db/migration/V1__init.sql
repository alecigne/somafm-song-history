CREATE TABLE IF NOT EXISTS songs
(
    id     SERIAL UNIQUE,
    artist VARCHAR(255),
    title  VARCHAR(255),
    album  VARCHAR(255),
    PRIMARY KEY (artist, title, album)
);

CREATE TABLE IF NOT EXISTS broadcasts
(
    utc_time TIMESTAMPTZ,
    channel  VARCHAR(255),
    song_id  INT REFERENCES songs(id),
    PRIMARY KEY (utc_time, channel)
);
