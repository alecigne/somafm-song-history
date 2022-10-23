package net.lecigne.somafm.business;

import java.io.IOException;
import java.time.ZoneId;
import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import net.lecigne.somafm.config.Configuration;
import net.lecigne.somafm.exception.SomaFmHtmlParsingException;
import net.lecigne.somafm.mappers.DisplayedBroadcastMapper;
import net.lecigne.somafm.model.Broadcast;
import net.lecigne.somafm.model.Channel;
import net.lecigne.somafm.repository.BroadcastRepository;
import net.lecigne.somafm.repository.DefaultBroadcastRepository;

@RequiredArgsConstructor
public class RecentBroadcastBusiness {

  private final BroadcastRepository broadcastRepository;
  private final DisplayedBroadcastMapper displayedBroadcastMapper;

  @SuppressWarnings("java:S106") // command line application - println is ok
  public void displayRecentBroadcasts(String publicName) {
    Channel channel = Channel.getByPublicName(publicName);
    try {
      broadcastRepository.getRecentBroadcasts(channel).stream()
          .sorted(Comparator.comparing(Broadcast::getTime).reversed())
          .map(displayedBroadcastMapper::map)
          .forEach(System.out::println);
    } catch (IOException e) {
      System.out.println("Unable to get recent broadcast from SomaFM at this time. Please try again later.");
    } catch (SomaFmHtmlParsingException e) {
      System.out.println(e.getMessage());
    }
  }

  public static RecentBroadcastBusiness init(Configuration config) {
    return new RecentBroadcastBusiness(
        DefaultBroadcastRepository.init(config),
        new DisplayedBroadcastMapper(ZoneId.systemDefault())
    );
  }

}
