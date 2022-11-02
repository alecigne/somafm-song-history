package net.lecigne.somafm.repository;

import java.io.IOException;
import java.time.Clock;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.lecigne.somafm.client.RecentBroadcastsClient;
import net.lecigne.somafm.client.dto.RecentBroadcastsDto;
import net.lecigne.somafm.config.Configuration;
import net.lecigne.somafm.mappers.BroadcastMapper;
import net.lecigne.somafm.model.Broadcast;
import net.lecigne.somafm.model.Channel;

/**
 * Handle most recent SomaFM broadcasts for a given channel.
 */
@AllArgsConstructor
@Slf4j
public class DefaultBroadcastRepository implements BroadcastRepository {

  private RecentBroadcastsClient recentBroadcastsClient;
  private BroadcastMapper broadcastMapper;

  @Override
  public Set<Broadcast> getRecentBroadcasts(Channel channel) throws IOException {
    log.info("Getting recent broadcasts for SomaFM's {}", channel.getPublicName());
    RecentBroadcastsDto recentBroadcastsDto = recentBroadcastsClient.get(channel);
    return broadcastMapper.map(recentBroadcastsDto);
  }

  public static BroadcastRepository init(Configuration config) {
    RecentBroadcastsClient client = RecentBroadcastsClient.init(config);
    BroadcastMapper mapper = new BroadcastMapper(Clock.systemDefaultZone());
    return new DefaultBroadcastRepository(client, mapper);
  }

}
