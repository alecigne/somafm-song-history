package net.lecigne.somafm.history.application.services;

import static net.lecigne.somafm.history.fixtures.Fixtures.breakSongFixture;
import static net.lecigne.somafm.history.fixtures.Fixtures.dirkSerriesSix;
import static net.lecigne.somafm.history.fixtures.Fixtures.igneousFlameIncandescentArc;
import static net.lecigne.somafm.recentlib.PredefinedChannel.DRONE_ZONE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import net.lecigne.somafm.history.application.ports.out.BroadcastRepository;
import net.lecigne.somafm.history.application.ports.out.SomaFmRepository;
import net.lecigne.somafm.history.domain.model.Broadcast;
import net.lecigne.somafm.history.domain.model.Song;
import net.lecigne.somafm.recentlib.Channel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The SomaFM recent service")
class SomaFmRecentServiceTest {

  private final FakeSomaFmRepository somaFmRepo = new FakeSomaFmRepository();
  private final FakeBroadcastRepository broadcastRepo = new FakeBroadcastRepository();
  private final SomaFmRecentService service = new SomaFmRecentService(somaFmRepo, broadcastRepo);

  @AfterEach
  void tearDown() {
    somaFmRepo.reset();
    broadcastRepo.reset();
  }

  @Test
  void should_fetch_and_sort_recent_broadcasts() {
    // Given
    Broadcast oldest = broadcastAt("2021-01-01T11:00:00.00Z", dirkSerriesSix());
    Broadcast newest = broadcastAt("2021-01-01T11:20:00.00Z", breakSongFixture());
    Broadcast middle = broadcastAt("2021-01-01T11:10:00.00Z", igneousFlameIncandescentArc());
    somaFmRepo.recentBroadcasts = List.of(oldest, newest, middle);

    // When
    List<Broadcast> recentBroadcasts = service.fetchRecent(DRONE_ZONE);

    // Then
    assertThat(recentBroadcasts).containsExactly(newest, middle, oldest);
  }

  @Test
  void should_save_recent_broadcasts() {
    // Given
    var first = broadcastAt("2021-01-01T11:00:00.00Z", dirkSerriesSix());
    var second = broadcastAt("2021-01-01T11:10:00.00Z", igneousFlameIncandescentArc());
    somaFmRepo.recentBroadcasts = List.of(first, second);

    // When
    List<Broadcast> recentBroadcasts = service.saveRecent(DRONE_ZONE);

    // Then
    assertThat(recentBroadcasts).containsExactly(first, second);
    assertThat(broadcastRepo.updatedBroadcasts).containsExactly(first, second);
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

    private List<Broadcast> updatedBroadcasts = List.of();

    @Override
    public long countBroadcasts() {
      return 0;
    }

    @Override
    public List<Broadcast> getBroadcasts(int page, int size) {
      return List.of();
    }

    @Override
    public void updateBroadcasts(List<Broadcast> broadcasts) {
      updatedBroadcasts = new ArrayList<>(broadcasts);
    }

    private void reset() {
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
