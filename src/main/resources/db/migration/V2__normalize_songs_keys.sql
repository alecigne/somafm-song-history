ALTER TABLE songs
ALTER COLUMN id SET NOT NULL,
ALTER COLUMN artist SET NOT NULL,
ALTER COLUMN title SET NOT NULL,
ALTER COLUMN album SET NOT NULL;

ALTER TABLE broadcasts
DROP CONSTRAINT IF EXISTS broadcasts_song_id_fkey;

ALTER TABLE songs
DROP CONSTRAINT songs_pkey;

ALTER TABLE songs
ADD CONSTRAINT songs_pkey PRIMARY KEY (id);

ALTER TABLE songs
ADD CONSTRAINT songs_artist_title_album_key UNIQUE (artist, title, album);

ALTER TABLE songs
DROP CONSTRAINT IF EXISTS songs_id_key;

ALTER TABLE broadcasts
ADD CONSTRAINT broadcasts_song_id_fkey FOREIGN KEY (song_id) REFERENCES songs(id);
