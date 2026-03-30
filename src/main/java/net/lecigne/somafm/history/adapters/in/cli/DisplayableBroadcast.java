package net.lecigne.somafm.history.adapters.in.cli;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
class DisplayableBroadcast {
  private String time;
  private String channel;
  private String artist;
  private String title;

  @Override
  public String toString() {
    return "[" + time + " @ " + channel + "] " + (artist != null ? artist + " - " : "") + title;
  }

}
