package net.lecigne.somafm.mappers;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import net.lecigne.somafm.model.Broadcast;
import net.lecigne.somafm.model.DisplayedBroadcast;

@RequiredArgsConstructor
public class DisplayedBroadcastMapper {

  private final ZoneId zoneId;
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  public DisplayedBroadcast map(Broadcast broadcast) {
    return DisplayedBroadcast.builder()
        .time(LocalDateTime.ofInstant(broadcast.getTime(), zoneId).format(FORMATTER))
        .channel(broadcast.getChannel().getPublicName())
        .artist(broadcast.getSong().getArtist())
        .title(broadcast.getSong().getTitle())
        .build();
  }

}
