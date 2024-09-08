package net.lecigne.somafm.model;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import net.lecigne.somafm.recentlib.Channel;

@Builder
@Getter
public class Broadcast {
  private Instant time;
  private Channel channel;
  private Song song;
}
