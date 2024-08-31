package net.lecigne.somafm.model;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Broadcast {
  private Instant time;
  private Channel channel;
  private Song song;
}
