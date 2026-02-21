package net.lecigne.somafm.history.adapters.in.cli;

import java.time.ZoneId;
import lombok.extern.slf4j.Slf4j;
import net.lecigne.somafm.history.application.ports.in.SomaFmSongHistory;
import net.lecigne.somafm.history.bootstrap.config.SomaFmConfig;
import net.lecigne.somafm.history.domain.Action;
import net.lecigne.somafm.history.domain.SomaFmCommand;
import net.lecigne.somafm.history.domain.UnknownChannelException;
import net.lecigne.somafm.recentlib.Channel;
import net.lecigne.somafm.recentlib.PredefinedChannel;
import net.lecigne.somafm.recentlib.SomaFmException;

@Slf4j
@SuppressWarnings("java:S106") // OK to log with System.out here
public class CLI {

  private final SomaFmSongHistory api;
  private final DisplayedBroadcastMapper displayedBroadcastMapper;

  CLI(SomaFmSongHistory api, DisplayedBroadcastMapper displayedBroadcastMapper) {
    this.api = api;
    this.displayedBroadcastMapper = displayedBroadcastMapper;
  }

  public void run(String[] args) {
    try {
      if (args.length != 2) {
        log.error("You must enter 2 arguments: action and channel.");
        return;
      }
      Action action = Action.getValue(args[0]);
      Channel channel = PredefinedChannel.getByPublicName(args[1])
                                         .orElseThrow(() -> new UnknownChannelException(args[1]));
      api.run(new SomaFmCommand(action, channel))
         .stream()
         .map(displayedBroadcastMapper::map)
         .forEach(System.out::println);
    } catch (UnknownChannelException e) {
      log.error("Unknown channel: {}", args[1]);
    } catch (SomaFmException e) {
      log.error("Error while fetching broadcasts.", e);
    } catch (Exception e) {
      log.error("Unexpected error.", e);
    }
  }

  public static CLI init(SomaFmSongHistory api, SomaFmConfig somaFmConfig) {
    return new CLI(api, new DisplayedBroadcastMapper(ZoneId.of(somaFmConfig.getTimezone())));
  }

}
