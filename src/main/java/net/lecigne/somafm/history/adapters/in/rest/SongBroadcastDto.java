package net.lecigne.somafm.history.adapters.in.rest;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import net.lecigne.somafm.history.domain.model.SongBroadcast;

record SongBroadcastDto(
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    Instant time,
    String channel
) {

  static SongBroadcastDto from(SongBroadcast broadcast) {
    return new SongBroadcastDto(broadcast.time(), broadcast.channel().publicName());
  }

}
