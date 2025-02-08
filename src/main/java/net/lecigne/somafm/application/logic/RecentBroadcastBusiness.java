package net.lecigne.somafm.application.logic;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.lecigne.somafm.application.api.SomaFmSongHistoryApi;
import net.lecigne.somafm.application.spi.SomaFmSongHistorySpi;
import net.lecigne.somafm.recentlib.Broadcast;
import net.lecigne.somafm.recentlib.Channel;

@Slf4j
public class RecentBroadcastBusiness implements SomaFmSongHistoryApi {

  private final SomaFmSongHistorySpi spi;

  public RecentBroadcastBusiness(SomaFmSongHistorySpi spi) {
    this.spi = spi;
  }

  @Override
  public List<Broadcast> fetchRecentBroadcasts(Channel channel) {
    List<Broadcast> recentBroadcasts = spi.getRecentBroadcasts(channel);
    log.info("Fetched {} last broadcasts from channel {}", recentBroadcasts.size(), channel.publicName());
    return recentBroadcasts;
  }

  @Override
  public List<Broadcast> saveRecentBroadcasts(Channel channel) {
    List<Broadcast> recentBroadcasts = spi.getRecentBroadcasts(channel);
    spi.updateBroadcasts(recentBroadcasts);
    log.info("Saved {} last broadcasts from channel {}", recentBroadcasts.size(), channel.publicName());
    return recentBroadcasts;
  }

}
