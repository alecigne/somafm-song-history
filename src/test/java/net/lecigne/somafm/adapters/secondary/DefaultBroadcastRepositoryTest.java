package net.lecigne.somafm.adapters.secondary;

import static net.lecigne.somafm.fixtures.TestFixtures.dirkSerriesSongFixture;
import static net.lecigne.somafm.recentlib.PredefinedChannel.DRONE_ZONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.Instant;
import java.util.List;
import net.lecigne.somafm.recentlib.Broadcast;
import net.lecigne.somafm.recentlib.SomaFm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@DisplayName("The broadcast repository")
class DefaultBroadcastRepositoryTest {

  private static final SomaFm somaFm = Mockito.mock(SomaFm.class);
  private static final DefaultBroadcastRepository repository = new DefaultBroadcastRepository(somaFm, null);

  @Test
  void should_get_most_recent_broadcasts() {
    // Given
    var channel = DRONE_ZONE;
    var broadcast = Broadcast.builder()
        .time(Instant.parse("2021-01-01T11:36:43.00Z"))
        .channel(channel)
        .song(dirkSerriesSongFixture())
        .build();

    given(somaFm.fetchRecent(DRONE_ZONE)).willReturn(List.of(broadcast));

    // When
    List<Broadcast> recentBroadcasts = repository.getRecentBroadcasts(channel);

    // Then
    assertThat(recentBroadcasts)
        .hasSize(1)
        .first()
        .usingRecursiveComparison()
        .isEqualTo(broadcast);
  }

}
