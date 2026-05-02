package net.lecigne.somafm.history.adapters.in.rest;

import net.lecigne.somafm.recentlib.Song;

record SongDto(String artist, String title, String album) {

  static SongDto from(Song song) {
    String artistName = song.artist() == null ? null : song.artist().name();
    return new SongDto(artistName, song.title(), song.album());
  }

}
