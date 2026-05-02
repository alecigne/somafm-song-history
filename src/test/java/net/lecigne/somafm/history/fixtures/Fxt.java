package net.lecigne.somafm.history.fixtures;

import net.lecigne.somafm.recentlib.Artist;
import net.lecigne.somafm.recentlib.Song;

public final class Fxt {

  public static Song dirkSerriesSix() {
    return Song.builder()
        .artist(Artist.builder().name("Dirk Serries' Microphonics").build())
        .title("VI")
        .album("microphonics VI - XX")
        .build();
  }

  public static Song igneousFlameIncandescentArc() {
    return Song.builder()
        .artist(Artist.builder().name("Igneous Flame").build())
        .title("Incandescent Arc")
        .album("Lapiz")
        .build();
  }

  public static Song igneousFlameRegenerativeShifts() {
    return Song.builder()
        .artist(Artist.builder().name("Igneous Flame").build())
        .title("Regenerative Shifts")
        .album("Lapiz").build();
  }

  public static Song breakSongFixture() {
    return Song.builder()
        .title("Break / Station ID")
        .build();
  }

  private Fxt() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

}
