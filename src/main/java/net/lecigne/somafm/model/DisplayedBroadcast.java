package net.lecigne.somafm.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class DisplayedBroadcast {
  private String time;
  private String channel;
  private String artist;
  private String title;

  @Override
  public String toString() {
    return "[" + time + " @ " + channel + "] " + (artist != null ? artist + " - " : "") + title;
  }

}
