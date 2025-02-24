package net.lecigne.somafm.adapters.primary;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.ZoneId;
import net.lecigne.somafm.domain.DisplayableBroadcast;
import net.lecigne.somafm.recentlib.Broadcast;
import net.lecigne.somafm.recentlib.PredefinedChannel;
import net.lecigne.somafm.recentlib.Song;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The displayed broadcast mapper")
class DisplayableBroadcastMapperTest {

  @Test
  void should_map_broadcast_to_displayable_format() {
    // Given
    var zoneId = ZoneId.of("Europe/Paris");
    var displayedBroadcastMapper = new DisplayedBroadcastMapper(zoneId);
    var broadcast = Broadcast.builder()
        .time(Instant.parse("2025-02-16T15:00:00Z"))
        .channel(PredefinedChannel.GROOVE_SALAD)
        .song(Song.builder()
            .artist("an_artist")
            .title("a_title")
            .album("an_album")
            .build())
        .build();

    var expectedBroadcast = DisplayableBroadcast.builder()
        .time("2025-02-16 16:00:00")
        .channel("Groove Salad")
        .artist("an_artist")
        .title("a_title")
        .build();

    // When
    DisplayableBroadcast displayableBroadcast = displayedBroadcastMapper.map(broadcast);

    // Then
    assertThat(displayableBroadcast).usingRecursiveComparison().isEqualTo(expectedBroadcast);
  }

}
