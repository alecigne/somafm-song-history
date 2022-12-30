package net.lecigne.somafm.business;

import static net.lecigne.somafm.business.BusinessAction.DISPLAY;
import static net.lecigne.somafm.business.BusinessAction.SAVE;
import static net.lecigne.somafm.model.Channel.DRONE_ZONE;

import java.io.IOException;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import net.lecigne.somafm.config.SomaFmConfig;
import net.lecigne.somafm.exception.SomaFmHtmlParsingException;
import net.lecigne.somafm.mappers.DisplayedBroadcastMapper;
import net.lecigne.somafm.model.Broadcast;
import net.lecigne.somafm.model.Channel;
import net.lecigne.somafm.repository.BroadcastRepository;
import net.lecigne.somafm.repository.DefaultBroadcastRepository;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@Slf4j
public class RecentBroadcastBusiness implements Job {

  private final BroadcastRepository broadcastRepository;
  private final DisplayedBroadcastMapper displayedBroadcastMapper;
  private final Map<BusinessAction, Consumer<Set<Broadcast>>> strategies;

  public RecentBroadcastBusiness(
      BroadcastRepository broadcastRepository,
      DisplayedBroadcastMapper displayedBroadcastMapper) {
    this.broadcastRepository = broadcastRepository;
    this.displayedBroadcastMapper = displayedBroadcastMapper;
    this.strategies = Map.of(DISPLAY, displayRecentBroadcasts(), SAVE, saveRecentBroadcasts());
  }

  @Override
  public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
    handleRecentBroadcasts(SAVE, DRONE_ZONE);
  }

  private void handleRecentBroadcasts(BusinessAction action, Channel channel) {
    try {
      Set<Broadcast> recentBroadcasts = broadcastRepository.getRecentBroadcasts(channel);
      strategies.get(action).accept(recentBroadcasts);
    } catch (IOException e) {
      log.error("Unable to get recent broadcasts from SomaFM at this time. Please try again later.", e);
    } catch (SomaFmHtmlParsingException e) {
      log.error("Unable to parse SomaFM recent broadcasts page.", e);
    }
  }

  private Consumer<Set<Broadcast>> saveRecentBroadcasts() {
    return broadcastRepository::updateBroadcasts;
  }

  private Consumer<Set<Broadcast>> displayRecentBroadcasts() {
    return broadcasts -> broadcasts.stream()
        .sorted(Comparator.comparing(Broadcast::getTime).reversed())
        .map(displayedBroadcastMapper::map)
        .forEach(System.out::println);
  }

  public static RecentBroadcastBusiness init(SomaFmConfig config) {
    return new RecentBroadcastBusiness(
        DefaultBroadcastRepository.init(config),
        new DisplayedBroadcastMapper(ZoneId.systemDefault())
    );
  }

}
