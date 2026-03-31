package net.lecigne.somafm.history.adapters.in.rest;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import net.lecigne.somafm.recentlib.Broadcast;
import net.lecigne.somafm.recentlib.Song;

record BroadcastResponseDto(
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    Instant time,
    String channel,
    SongDto song
) {

  static BroadcastResponseDto from(Broadcast broadcast) {
    Song song = broadcast.song();
    String artistName = song.artist() == null ? null : song.artist().name();
    return new BroadcastResponseDto(
        broadcast.time(),
        broadcast.channel().publicName(),
        new SongDto(artistName, song.title(), song.album()));
  }

  public record SongDto(String artist, String title, String album) {
  }

}
