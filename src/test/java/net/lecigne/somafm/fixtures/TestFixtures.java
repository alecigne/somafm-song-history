package net.lecigne.somafm.fixtures;

import static net.lecigne.somafm.SomaFmSongHistory.BREAK_STATION_ID;

import net.lecigne.somafm.model.Song;

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
        .title(BREAK_STATION_ID)
        .build();
  }

  private TestFixtures() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

}
