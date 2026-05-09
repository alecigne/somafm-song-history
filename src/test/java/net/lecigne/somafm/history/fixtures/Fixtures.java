package net.lecigne.somafm.history.fixtures;

import net.lecigne.somafm.history.domain.model.Song;
import net.lecigne.somafm.recentlib.Artist;

public final class Fixtures {

  public static Song dirkSerriesSix() {
    return Song.builder()
        .artist("Dirk Serries' Microphonics")
        .title("VI")
        .album("microphonics VI - XX")
        .build();
  }

  public static net.lecigne.somafm.recentlib.Song dirkSerriesSixDto() {
    return net.lecigne.somafm.recentlib.Song.builder()
        .artist(Artist.builder().name(dirkSerriesSix().artist()).build())
        .title(dirkSerriesSix().title())
        .album(dirkSerriesSix().album())
        .build();
  }

  public static Song igneousFlameIncandescentArc() {
    return Song.builder()
        .artist("Igneous Flame")
        .title("Incandescent Arc")
        .album("Lapiz")
        .build();
  }

  public static net.lecigne.somafm.recentlib.Song igneousFlameIncandescentArcDto() {
    return net.lecigne.somafm.recentlib.Song.builder()
        .artist(Artist.builder().name(igneousFlameIncandescentArc().artist()).build())
        .title(igneousFlameIncandescentArc().title())
        .album(igneousFlameIncandescentArc().album())
        .build();
  }

  public static Song igneousFlameRegenerativeShifts() {
    return Song.builder()
        .artist("Igneous Flame")
        .title("Regenerative Shifts")
        .album("Lapiz").build();
  }

  public static Song breakSongFixture() {
    return Song.builder().title("Break / Station ID").build();
  }

  public static net.lecigne.somafm.recentlib.Song breakSongFixtureDto() {
    return net.lecigne.somafm.recentlib.Song.builder().title(breakSongFixture().title()).build();
  }

  private Fixtures() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

}
