package net.lecigne.somafm.history.adapters.in.rest;

import java.util.List;
import net.lecigne.somafm.history.domain.model.Song;
import net.lecigne.somafm.history.domain.model.SongDetails;

record SongDetailsDto(
    Long id,
    String artist,
    String title,
    String album,
    List<SongBroadcastDto> broadcasts
) {

  static SongDetailsDto from(SongDetails details) {
    Song song = details.song();
    List<SongBroadcastDto> songBroadcastDtos = details.broadcasts().stream().map(SongBroadcastDto::from).toList();
    return new SongDetailsDto(
        song.id(),
        song.artist(),
        song.title(),
        song.album(),
        songBroadcastDtos);
  }

}
