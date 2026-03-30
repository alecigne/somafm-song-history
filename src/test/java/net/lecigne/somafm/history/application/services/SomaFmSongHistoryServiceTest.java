package net.lecigne.somafm.history.application.services;

import static net.lecigne.somafm.history.fixtures.TestFixtures.breakSongFixture;
import static net.lecigne.somafm.history.fixtures.TestFixtures.dirkSerriesSongFixture;
import static net.lecigne.somafm.history.fixtures.TestFixtures.igneousFlameSongFixture;
import static net.lecigne.somafm.recentlib.PredefinedChannel.DRONE_ZONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import net.lecigne.somafm.history.application.ports.out.BroadcastRepository;
import net.lecigne.somafm.history.application.ports.out.SomaFmRepository;
import net.lecigne.somafm.history.domain.model.Mode;
import net.lecigne.somafm.history.domain.model.Page;
import net.lecigne.somafm.history.domain.model.SomaFmCommand;
import net.lecigne.somafm.history.fixtures.TestFixtures;
import net.lecigne.somafm.recentlib.Broadcast;
import net.lecigne.somafm.recentlib.Channel;
import net.lecigne.somafm.recentlib.Song;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("The recent broadcast business")
class SomaFmSongHistoryServiceTest {

  private final FakeSomaFmRepository somaFmRepo = new FakeSomaFmRepository();
  private final FakeBroadcastRepository broadcastRepo = new FakeBroadcastRepository();
  private final SomaFmSongHistoryService service = new SomaFmSongHistoryService(somaFmRepo, broadcastRepo);

  @AfterEach
  void tearDown() {
    somaFmRepo.reset();
    broadcastRepo.reset();
  }

  @Nested
  class when_retrieving_broadcasts {

    @Test
    void should_get_paginated_broadcasts() {
      // Given
      var totalElements = 17L;
      broadcastRepo.totalElements = totalElements;

      var page = 1;
      var size = 2;
      List<Broadcast> broadcasts = List.of(
          broadcastAt("2021-01-01T11:00:00.00Z", TestFixtures.dirkSerriesSongFixture()),
          broadcastAt("2021-01-01T11:10:00.00Z", TestFixtures.igneousFlameSongFixture()));
      broadcastRepo.broadcasts = broadcasts;

      var expectedSize = 9; // 17 results, 2 per page -> 9 pages
      Page<Broadcast> broadcastPage = new Page<>(page, size, totalElements, expectedSize, broadcasts);

      // When
      Page<Broadcast> result = service.getBroadcasts(page, size);

      // Then
      assertThat(result).usingRecursiveComparison().isEqualTo(broadcastPage);
    }

    // See validator unit tests for specific cases
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

  @Test
  void should_fetch_and_sort_recent_broadcasts_with_display_action() {
    // Given
    Broadcast oldest = broadcastAt("2021-01-01T11:00:00.00Z", dirkSerriesSongFixture());
    Broadcast newest = broadcastAt("2021-01-01T11:20:00.00Z", breakSongFixture());
    Broadcast middle = broadcastAt("2021-01-01T11:10:00.00Z", igneousFlameSongFixture());
    somaFmRepo.recentBroadcasts = List.of(oldest, newest, middle);
    var command = new SomaFmCommand(Mode.DISPLAY, DRONE_ZONE);

    // When
    List<Broadcast> recentBroadcasts = service.runCommand(command);

    // Then
    assertThat(recentBroadcasts).containsExactly(newest, middle, oldest);
  }

  @Test
  void should_save_recent_broadcasts_with_save_action() {
    // Given
    var first = broadcastAt("2021-01-01T11:00:00.00Z", dirkSerriesSongFixture());
    var second = broadcastAt("2021-01-01T11:10:00.00Z", igneousFlameSongFixture());
    somaFmRepo.recentBroadcasts = List.of(first, second);
    var command = new SomaFmCommand(Mode.SAVE, DRONE_ZONE);

    // When
    List<Broadcast> recentBroadcasts = service.runCommand(command);

    // Then
    assertThat(recentBroadcasts).containsExactly(first, second);
    assertThat(broadcastRepo.updatedBroadcasts).containsExactly(first, second);
  }

  @Test
  void should_get_paginated_broadcasts() {
    // Given
    var first = broadcastAt("2021-01-01T11:00:00.00Z", dirkSerriesSongFixture());
    var second = broadcastAt("2021-01-01T11:10:00.00Z", igneousFlameSongFixture());
    broadcastRepo.broadcasts = List.of(first, second);
    broadcastRepo.totalElements = 17;

    // When
    var result = service.getBroadcasts(1, 2);

    // Then
    assertThat(result.number()).isEqualTo(1);
    assertThat(result.size()).isEqualTo(2);
    assertThat(result.totalElements()).isEqualTo(17);
    assertThat(result.totalPages()).isEqualTo(9);
    assertThat(result.items()).containsExactly(first, second);
  }

  @Test
  void should_reject_page_size_above_max() {
    // Then
    assertThatThrownBy(() -> service.getBroadcasts(1, 201))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("size must be <= 50");
  }

  private static class FakeSomaFmRepository implements SomaFmRepository {

    private List<Broadcast> recentBroadcasts = List.of();

    @Override
    public List<Broadcast> fetchRecentBroadcasts(Channel channel) {
      return recentBroadcasts;
    }

    private void reset() {
      recentBroadcasts = List.of();
    }
  }

  private static class FakeBroadcastRepository implements BroadcastRepository {

    private long totalElements;
    private List<Broadcast> broadcasts = List.of();
    private List<Broadcast> updatedBroadcasts = List.of();

    @Override
    public long countBroadcasts() {
      return totalElements;
    }

    @Override
    public List<Broadcast> getBroadcasts(int page, int size) {
      return broadcasts;
    }

    @Override
    public void updateBroadcasts(List<Broadcast> broadcasts) {
      updatedBroadcasts = new ArrayList<>(broadcasts);
    }

    private void reset() {
      totalElements = 0;
      broadcasts = List.of();
      updatedBroadcasts = List.of();
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
