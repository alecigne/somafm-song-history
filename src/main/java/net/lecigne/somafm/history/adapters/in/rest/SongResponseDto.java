package net.lecigne.somafm.history.adapters.in.rest;

import net.lecigne.somafm.recentlib.Song;

record SongResponseDto(String artist, String title, String album) {

  static SongResponseDto from(Song song) {
    String artistName = song.artist() == null ? null : song.artist().name();
    return new SongResponseDto(artistName, song.title(), song.album());
  }

}
