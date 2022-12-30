package net.lecigne.somafm.business;

import java.io.IOException;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import net.lecigne.somafm.config.SomaFmConfig;
import net.lecigne.somafm.exception.SomaFmHtmlParsingException;
import net.lecigne.somafm.exception.UnknownChannelException;
import net.lecigne.somafm.model.Broadcast;
import net.lecigne.somafm.model.Channel;
import net.lecigne.somafm.repository.BroadcastRepository;
import net.lecigne.somafm.repository.DefaultBroadcastRepository;

@Slf4j
public class RecentBroadcastBusiness {

  private final BroadcastRepository broadcastRepository;

  public RecentBroadcastBusiness(BroadcastRepository broadcastRepository) {
    this.broadcastRepository = broadcastRepository;
  }

  public void handleRecentBroadcasts(String channelName) {
    Channel channel = Channel.getByPublicName(channelName).orElseThrow(() -> new UnknownChannelException(channelName));
    try {
      Set<Broadcast> recentBroadcasts = broadcastRepository.getRecentBroadcasts(channel);
      broadcastRepository.updateBroadcasts(recentBroadcasts);
    } catch (IOException e) {
      log.error("Unable to get recent broadcasts from SomaFM at this time. Please try again later.", e);
    } catch (SomaFmHtmlParsingException e) {
      log.error("Unable to parse SomaFM recent broadcasts page.", e);
    }
  }

  public static RecentBroadcastBusiness init(SomaFmConfig config) {
    return new RecentBroadcastBusiness(DefaultBroadcastRepository.init(config));
  }

}
