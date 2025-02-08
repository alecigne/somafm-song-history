package net.lecigne.somafm.fixtures;

import net.lecigne.somafm.recentlib.Song;

public final class TestFixtures {

  public static Song dirkSerriesSongFixture() {
    return Song.builder()
        .artist("Dirk Serries' Microphonics")
        .title("VI")
        .album("microphonics VI - XX")
        .build();
  }

  public static Song igneousFlameSongFixture() {
    return Song.builder()
        .artist("Igneous Flame")
        .title("Incandescent Arc")
        .album("Lapiz")
        .build();
  }

  public static Song breakSongFixture() {
    return Song.builder()
        .title("Break / Station ID")
        .build();
  }

  private TestFixtures() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

}
