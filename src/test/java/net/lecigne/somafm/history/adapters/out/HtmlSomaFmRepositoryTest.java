package net.lecigne.somafm.history.adapters.out;

import static net.lecigne.somafm.recentlib.PredefinedChannel.DRONE_ZONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.Instant;
import java.util.List;
import net.lecigne.somafm.history.domain.model.Broadcast;
import net.lecigne.somafm.history.domain.model.Song;
import net.lecigne.somafm.recentlib.Artist;
import net.lecigne.somafm.recentlib.SomaFm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@DisplayName("The broadcast repository")
class HtmlSomaFmRepositoryTest {

  private static final SomaFm somaFm = Mockito.mock(SomaFm.class);
  private static final HtmlSomaFmRepository htmlSomaFmRepo = new HtmlSomaFmRepository(somaFm);

  @Test
  void should_get_most_recent_broadcasts() {
    // Given
    var channel = DRONE_ZONE;
    var broadcastDto = net.lecigne.somafm.recentlib.Broadcast.builder()
        .time(Instant.parse("2021-01-01T11:36:43.00Z"))
        .channel(channel)
        .song(net.lecigne.somafm.recentlib.Song.builder()
            .artist(Artist.builder().name("Dirk Serries' Microphonics").build())
            .title("VI")
            .album("microphonics VI - XX")
            .build())
        .build();

    var expectedBroadcast = Broadcast.builder()
        .time(broadcastDto.time())
        .channel(broadcastDto.channel())
        .song(Song.builder()
            .artist(broadcastDto.song().artist().name())
            .title(broadcastDto.song().title())
            .album(broadcastDto.song().album())
            .build())
        .build();

    given(somaFm.fetchRecent(DRONE_ZONE)).willReturn(List.of(broadcastDto));

    // When
    List<Broadcast> recentBroadcasts = htmlSomaFmRepo.fetchRecentBroadcasts(channel);

    // Then
    assertThat(recentBroadcasts)
        .hasSize(1)
        .first()
        .usingRecursiveComparison()
        .isEqualTo(expectedBroadcast);
  }

}
