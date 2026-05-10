package net.lecigne.somafm.history.adapters.in.rest;

import net.lecigne.somafm.history.domain.model.Song;

record SongDto(Long id, String artist, String title, String album) {

  static SongDto from(Song song) {
    return new SongDto(song.id(), song.artist(), song.title(), song.album());
  }

}
