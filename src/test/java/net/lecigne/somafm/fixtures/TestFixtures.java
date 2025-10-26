package net.lecigne.somafm.fixtures;

import net.lecigne.somafm.recentlib.Artist;
import net.lecigne.somafm.recentlib.Song;

public final class TestFixtures {

  public static Song dirkSerriesSongFixture() {
    return Song.builder()
        .artist(Artist.builder().name("Dirk Serries' Microphonics").build())
        .title("VI")
        .album("microphonics VI - XX")
        .build();
  }

  public static Song igneousFlameSongFixture() {
    return Song.builder()
        .artist(Artist.builder().name("Igneous Flame").build())
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
