package net.lecigne.somafm.history.application.services;

import static net.lecigne.somafm.history.fixtures.TestFixtures.breakSongFixture;
import static net.lecigne.somafm.history.fixtures.TestFixtures.dirkSerriesSongFixture;
import static net.lecigne.somafm.history.fixtures.TestFixtures.igneousFlameSongFixture;
import static net.lecigne.somafm.recentlib.PredefinedChannel.DRONE_ZONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import net.lecigne.somafm.history.application.ports.out.BroadcastRepository;
import net.lecigne.somafm.history.application.ports.out.SomaFmRepository;
import net.lecigne.somafm.history.domain.model.Mode;
import net.lecigne.somafm.history.domain.model.Page;
import net.lecigne.somafm.history.domain.model.SomaFmCommand;
import net.lecigne.somafm.history.fixtures.TestFixtures;
import net.lecigne.somafm.recentlib.Broadcast;
import net.lecigne.somafm.recentlib.Song;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@DisplayName("The recent broadcast business")
class SomaFmSongHistoryServiceTest {

  private final SomaFmRepository somaFmRepo = Mockito.mock(SomaFmRepository.class);
  private final BroadcastRepository broadcastRepo = Mockito.mock(BroadcastRepository.class);
  private final SomaFmSongHistoryService service = new SomaFmSongHistoryService(somaFmRepo, broadcastRepo);

  @AfterEach
  void tearDown() {
    Mockito.reset(somaFmRepo, broadcastRepo);
  }

  @Nested
  class when_retrieving_broadcasts {

    @Test
    void should_get_paginated_broadcasts() {
      // Given
      var totalElements = 17L;
      given(broadcastRepo.countBroadcasts()).willReturn(totalElements);

      var page = 1;
      var size = 2;
      List<Broadcast> broadcasts = List.of(
          broadcastAt("2021-01-01T11:00:00.00Z", TestFixtures.dirkSerriesSongFixture()),
          broadcastAt("2021-01-01T11:10:00.00Z", TestFixtures.igneousFlameSongFixture()));
      given(broadcastRepo.getBroadcasts(page, size)).willReturn(broadcasts);

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
    SomaFmRepository somaFmRepo = channel -> List.of(oldest, newest, middle);
    var recentBroadcastBusiness = new SomaFmSongHistoryService(somaFmRepo, null);
    var command = new SomaFmCommand(Mode.DISPLAY, DRONE_ZONE);

    // When
    List<Broadcast> recentBroadcasts = recentBroadcastBusiness.runCommand(command);

    // Then
    assertThat(recentBroadcasts).containsExactly(newest, middle, oldest);
  }

  @Test
  void should_save_recent_broadcasts_with_save_action() {
    // Given
    var first = broadcastAt("2021-01-01T11:00:00.00Z", dirkSerriesSongFixture());
    var second = broadcastAt("2021-01-01T11:10:00.00Z", igneousFlameSongFixture());
    var broadcasts = List.of(first, second);
    SomaFmRepository somaFmRepo = channel -> broadcasts;
    AtomicReference<List<Broadcast>> persistedBroadcasts = new AtomicReference<>();
    BroadcastRepository broadcastRepo = new BroadcastRepository() {
      @Override
      public List<Broadcast> getBroadcasts(int page, int size) {
        return List.of();
      }

      @Override
      public long countBroadcasts() {
        return 0;
      }

      @Override
      public void updateBroadcasts(List<Broadcast> broadcastsToSave) {
        persistedBroadcasts.set(broadcastsToSave);
      }
    };
    var recentBroadcastBusiness = new SomaFmSongHistoryService(somaFmRepo, broadcastRepo);
    var command = new SomaFmCommand(Mode.SAVE, DRONE_ZONE);

    // When
    List<Broadcast> recentBroadcasts = recentBroadcastBusiness.runCommand(command);

    // Then
    assertThat(recentBroadcasts).containsExactly(first, second);
    assertThat(persistedBroadcasts.get()).containsExactly(first, second);
  }

  @Test
  void should_get_paginated_broadcasts() {
    // Given
    var first = broadcastAt("2021-01-01T11:00:00.00Z", dirkSerriesSongFixture());
    var second = broadcastAt("2021-01-01T11:10:00.00Z", igneousFlameSongFixture());
    var broadcasts = List.of(first, second);
    BroadcastRepository broadcastRepo = new BroadcastRepository() {
      @Override
      public void updateBroadcasts(List<Broadcast> ignored) {
      }

      @Override
      public List<Broadcast> getBroadcasts(int page, int size) {
        return broadcasts;
      }

      @Override
      public long countBroadcasts() {
        return 17;
      }
    };
    SomaFmRepository somaFmRepo = channel -> List.of();
    var recentBroadcastBusiness = new SomaFmSongHistoryService(somaFmRepo, broadcastRepo);

    // When
    var result = recentBroadcastBusiness.getBroadcasts(1, 2);

    // Then
    assertThat(result.number()).isEqualTo(1);
    assertThat(result.size()).isEqualTo(2);
    assertThat(result.totalElements()).isEqualTo(17);
    assertThat(result.totalPages()).isEqualTo(9);
    assertThat(result.items()).containsExactly(first, second);
  }

  @Test
  void should_reject_page_size_above_max() {
    // Given
    BroadcastRepository broadcastRepo = new BroadcastRepository() {
      @Override
      public void updateBroadcasts(List<Broadcast> ignored) {
      }

      @Override
      public List<Broadcast> getBroadcasts(int page, int size) {
        return List.of();
      }

      @Override
      public long countBroadcasts() {
        return 0;
      }
    };
    SomaFmRepository somaFmRepo = channel -> List.of();
    var recentBroadcastBusiness = new SomaFmSongHistoryService(somaFmRepo, broadcastRepo);

    // Then
    assertThatThrownBy(() -> recentBroadcastBusiness.getBroadcasts(1, 201))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("size must be <= 200");
  }

  private static Broadcast broadcastAt(String time, Song song) {
    return Broadcast.builder()
        .time(Instant.parse(time))
        .channel(DRONE_ZONE)
        .song(song)
        .build();
  }

}
