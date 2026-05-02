package net.lecigne.somafm.history.application.services;

import static net.lecigne.somafm.recentlib.PredefinedChannel.DRONE_ZONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import net.lecigne.somafm.history.application.ports.out.BroadcastRepository;
import net.lecigne.somafm.history.application.ports.out.SongRepository;
import net.lecigne.somafm.history.domain.model.Page;
import net.lecigne.somafm.history.fixtures.Fxt;
import net.lecigne.somafm.recentlib.Broadcast;
import net.lecigne.somafm.recentlib.Song;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("The SomaFM history service")
class SomaFmHistoryServiceTest {

  private final FakeBroadcastRepository broadcastRepo = new FakeBroadcastRepository();
  private final FakeSongRepository songRepo = new FakeSongRepository();
  private final SomaFmHistoryService service = new SomaFmHistoryService(broadcastRepo, songRepo);

  @AfterEach
  void tearDown() {
    broadcastRepo.reset();
    songRepo.reset();
  }

  @Nested
  class when_retrieving_broadcasts {

    @Test
    void should_get_paginated_broadcasts() {
      // Given
      var totalNumberOfBroadcasts = 17L;
      broadcastRepo.totalNumberOfBroadcasts = totalNumberOfBroadcasts;

      var page = 1;
      var size = 2;
      List<Broadcast> broadcastsInPage = List.of(
          broadcastAt("2021-01-01T11:00:00.00Z", Fxt.dirkSerriesSix()),
          broadcastAt("2021-01-01T11:10:00.00Z", Fxt.igneousFlameIncandescentArc()));
      broadcastRepo.broadcastsInPage = broadcastsInPage;

      var numberOfPages = 9; // 17 results, 2 per page -> 8 full pages + 1 extra page
      Page<Broadcast> broadcastPage = new Page<>(page, size, totalNumberOfBroadcasts, numberOfPages, broadcastsInPage);

      // When
      Page<Broadcast> result = service.getBroadcasts(page, size);

      // Then
      assertThat(result).usingRecursiveComparison().isEqualTo(broadcastPage);
    }

    // See pagination service unit tests for specific cases
    @Test
    void should_propagate_validation_error() {
      // Given
      var page = 1;
      var size = 51;
      var errMsg = "size must be <= 50";

      // "When"
      ThrowingCallable call = () -> service.getBroadcasts(page, size);

      // Then
      assertThatThrownBy(call)
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage(errMsg);
    }

  }

  @Nested
  class when_retrieving_songs {

    @Test
    void should_get_paginated_songs() {
      // Given
      var totalNumberOfSong = 17L;
      songRepo.totalNumberOfSongs = totalNumberOfSong;

      var page = 1;
      var size = 2;
      List<Song> songsInPage = List.of(Fxt.dirkSerriesSix(), Fxt.igneousFlameIncandescentArc());
      songRepo.songsInPage = songsInPage;

      var numberOfPages = 9; // 17 results, 2 per page -> 8 full pages + 1 extra page
      Page<Song> pageOfSongs = new Page<>(page, size, totalNumberOfSong, numberOfPages, songsInPage);

      // When
      Page<Song> result = service.getSongs(page, size);

      // Then
      assertThat(result).usingRecursiveComparison().isEqualTo(pageOfSongs);
    }

    // See pagination service unit tests for specific cases
    @Test
    void should_propagate_validation_error() {
      // Given
      var page = 1;
      var size = 51;
      var errMsg = "size must be <= 50";

      // "When"
      ThrowingCallable call = () -> service.getSongs(page, size);

      // Then
      assertThatThrownBy(call)
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage(errMsg);
    }

  }

  private static class FakeBroadcastRepository implements BroadcastRepository {

    private long totalNumberOfBroadcasts;
    private List<Broadcast> broadcastsInPage = List.of();

    @Override
    public long countBroadcasts() {
      return totalNumberOfBroadcasts;
    }

    @Override
    public List<Broadcast> getBroadcasts(int page, int size) {
      return broadcastsInPage;
    }

    @Override
    public void updateBroadcasts(List<Broadcast> broadcasts) {
      // Unused in history tests
    }

    private void reset() {
      totalNumberOfBroadcasts = 0;
      broadcastsInPage = List.of();
    }
  }

  private static class FakeSongRepository implements SongRepository {

    private long totalNumberOfSongs;
    private List<Song> songsInPage = List.of();

    @Override
    public long countSongs() {
      return totalNumberOfSongs;
    }

    @Override
    public List<Song> getSongs(int page, int size) {
      return songsInPage;
    }

    private void reset() {
      totalNumberOfSongs = 0;
      songsInPage = List.of();
    }

  }

  private static Broadcast broadcastAt(String time, Song song) {
    return Broadcast.builder()
        .time(Instant.parse(time))
        .channel(DRONE_ZONE)
        .song(song)
        .build();
  }

}
