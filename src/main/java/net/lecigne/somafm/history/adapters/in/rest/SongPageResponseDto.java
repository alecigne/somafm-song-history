package net.lecigne.somafm.history.adapters.in.rest;

import java.util.List;
import net.lecigne.somafm.history.domain.model.Page;
import net.lecigne.somafm.recentlib.Song;

record SongPageResponseDto(
    int page,
    int size,
    long totalElements,
    int totalPages,
    List<SongResponseDto> items
) {

  static SongPageResponseDto from(Page<Song> songPage) {
    List<SongResponseDto> songs = songPage.items()
        .stream()
        .map(SongResponseDto::from)
        .toList();
    return new SongPageResponseDto(
        songPage.number(),
        songPage.size(),
        songPage.totalElements(),
        songPage.totalPages(),
        songs);
  }

}
