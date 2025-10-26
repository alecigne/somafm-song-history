package net.lecigne.somafm.adapters.primary;

import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lecigne.somafm.application.api.SomaFmSongHistoryApi;
import net.lecigne.somafm.config.SomaFmConfig;
import net.lecigne.somafm.domain.Action;
import net.lecigne.somafm.domain.UnknownChannelException;
import net.lecigne.somafm.recentlib.Broadcast;
import net.lecigne.somafm.recentlib.Channel;
import net.lecigne.somafm.recentlib.PredefinedChannel;
import net.lecigne.somafm.recentlib.SomaFmException;

@Slf4j
@RequiredArgsConstructor
public class CLI {

  private final SomaFmSongHistoryApi api;
  private final DisplayedBroadcastMapper displayedBroadcastMapper;

  public void run(String[] args) {
    try {
      checkArgs(args);
      Action action = Action.getValue(args[0]);
      Channel channel = PredefinedChannel
          .getByPublicName(args[1])
          .orElseThrow(() -> new UnknownChannelException(args[1]));
      if (action == Action.DISPLAY) {
        fetchAndDisplay(channel);
      } else if (action == Action.SAVE) {
        saveAndDisplay(channel);
      }
    } catch (UnknownChannelException e) {
      log.error("Unknown channel: {}", args[1]);
    } catch (IllegalArgumentException e) {
      log.error("You must enter two arguments - action and channel.");
    } catch (SomaFmException e) {
      log.error("Error while fetching broadcasts.", e);
    } catch (Exception e) {
      log.error("Unexpected error.", e);
    }
  }

  private void doAndDisplay(Channel channel, Function<Channel, List<Broadcast>> action) {
    action.apply(channel)
        .stream()
        .sorted(Comparator.comparing(Broadcast::time).reversed())
        .map(displayedBroadcastMapper::map)
        .forEach(System.out::println);
  }

  private void fetchAndDisplay(Channel channel) {
    log.info("Displaying recent broadcasts from {}", channel);
    doAndDisplay(channel, api::fetchRecentBroadcasts);
  }

  private void saveAndDisplay(Channel channel) {
    log.info("Saving recent broadcasts from {}", channel);
    doAndDisplay(channel, api::saveRecentBroadcasts);
    log.info("Saved {} recent broadcasts", channel);
  }

  private static void checkArgs(String[] args) {
    if (args.length != 2) {
      throw new IllegalArgumentException();
    }
  }

  public static CLI initCli(SomaFmSongHistoryApi api, SomaFmConfig somaFmConfig) {
    return new CLI(api, new DisplayedBroadcastMapper(ZoneId.of(somaFmConfig.getTimezone())));
  }

}
