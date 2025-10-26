package net.lecigne.somafm.adapters.primary;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import net.lecigne.somafm.domain.DisplayableBroadcast;
import net.lecigne.somafm.recentlib.Broadcast;

@RequiredArgsConstructor
class DisplayedBroadcastMapper {

  private final ZoneId zoneId;
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  DisplayableBroadcast map(Broadcast broadcast) {
    return DisplayableBroadcast.builder()
        .time(LocalDateTime.ofInstant(broadcast.time(), zoneId).format(FORMATTER))
        .channel(broadcast.channel().publicName())
        .artist(broadcast.song().artist() != null ? broadcast.song().artist().name() : null)
        .title(broadcast.song().title())
        .build();
  }

}
