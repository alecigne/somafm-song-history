package net.lecigne.somafm.mappers;

import static net.lecigne.somafm.SomaFmSongHistory.BROADCAST_LOCATION;

import java.time.Clock;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import net.lecigne.somafm.client.dto.RecentBroadcastsDto;
import net.lecigne.somafm.model.Broadcast;
import net.lecigne.somafm.model.Channel;
import net.lecigne.somafm.model.Song;
import net.lecigne.somafm.utils.TimeUtils;

@RequiredArgsConstructor
public class BroadcastMapper {

  private final Clock clock;

  public Set<Broadcast> map(RecentBroadcastsDto recentBroadcastsDto) {
    Channel channel = Channel.getByInternalName(recentBroadcastsDto.getChannel());
    return recentBroadcastsDto.getRecentBroadcasts().stream()
        .map(dto -> Broadcast.builder()
            .time(TimeUtils.localBroadcastTimeToInstant(dto.getTime(), Instant.now(clock), BROADCAST_LOCATION))
            .channel(channel)
            .song(Song.builder()
                .artist(dto.getArtist())
                .title(dto.getTitle())
                .album(dto.getAlbum())
                .build())
            .build())
        .collect(Collectors.toSet());
  }

}
