package net.lecigne.somafm.history.adapters.in.rest;

import java.util.List;
import net.lecigne.somafm.history.application.model.Page;
import net.lecigne.somafm.history.domain.model.Song;

record SongPageDto(
    int page,
    int size,
    long totalElements,
    int totalPages,
    List<SongDto> items
) {

  static SongPageDto from(Page<Song> songPage) {
    List<SongDto> songs = songPage.items().stream().map(SongDto::from).toList();
    return new SongPageDto(
        songPage.number(),
        songPage.size(),
        songPage.totalElements(),
        songPage.totalPages(),
        songs);
  }

}
