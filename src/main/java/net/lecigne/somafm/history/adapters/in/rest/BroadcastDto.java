package net.lecigne.somafm.history.adapters.in.rest;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import net.lecigne.somafm.recentlib.Broadcast;

record BroadcastDto(
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    Instant time,
    String channel,
    SongDto song
) {

  static BroadcastDto from(Broadcast broadcast) {
    return new BroadcastDto(broadcast.time(), broadcast.channel().publicName(), SongDto.from(broadcast.song()));
  }

}
