package net.lecigne.somafm.history.application.services;

import static net.lecigne.somafm.history.fixtures.TestFixtures.breakSongFixture;
import static net.lecigne.somafm.history.fixtures.TestFixtures.dirkSerriesSongFixture;
import static net.lecigne.somafm.history.fixtures.TestFixtures.igneousFlameSongFixture;
import static net.lecigne.somafm.recentlib.PredefinedChannel.DRONE_ZONE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import net.lecigne.somafm.history.application.ports.out.BroadcastRepository;
import net.lecigne.somafm.history.application.ports.out.SomaFmRepository;
import net.lecigne.somafm.history.application.services.DefaultSomaFmSongHistory;
import net.lecigne.somafm.history.domain.Action;
import net.lecigne.somafm.history.domain.SomaFmCommand;
import net.lecigne.somafm.recentlib.Broadcast;
import net.lecigne.somafm.recentlib.Song;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The recent broadcast business")
class DefaultSomaFmSongHistoryTest {

  @Test
  void should_fetch_and_sort_recent_broadcasts_with_display_action() {
    // Given
    Broadcast oldest = broadcastAt("2021-01-01T11:00:00.00Z", dirkSerriesSongFixture());
    Broadcast newest = broadcastAt("2021-01-01T11:20:00.00Z", breakSongFixture());
    Broadcast middle = broadcastAt("2021-01-01T11:10:00.00Z", igneousFlameSongFixture());
    SomaFmRepository somaFmRepo = channel -> List.of(oldest, newest, middle);
    var recentBroadcastBusiness = new DefaultSomaFmSongHistory(somaFmRepo, null);
    var command = new SomaFmCommand(Action.DISPLAY, DRONE_ZONE);

    // When
    List<Broadcast> recentBroadcasts = recentBroadcastBusiness.run(command);

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
    BroadcastRepository broadcastRepo = persistedBroadcasts::set;
    var recentBroadcastBusiness = new DefaultSomaFmSongHistory(somaFmRepo, broadcastRepo);
    var command = new SomaFmCommand(Action.SAVE, DRONE_ZONE);

    // When
    List<Broadcast> recentBroadcasts = recentBroadcastBusiness.run(command);

    // Then
    assertThat(recentBroadcasts).containsExactly(first, second);
    assertThat(persistedBroadcasts.get()).containsExactly(first, second);
  }

  private static Broadcast broadcastAt(String time, Song song) {
    return Broadcast.builder()
                    .time(Instant.parse(time))
                    .channel(DRONE_ZONE)
                    .song(song)
                    .build();
  }

}
