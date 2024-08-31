package net.lecigne.somafm.cli;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lecigne.somafm.business.BusinessAction;
import net.lecigne.somafm.business.RecentBroadcastBusiness;
import net.lecigne.somafm.exception.UnknownChannelException;
import net.lecigne.somafm.model.Channel;

@Slf4j
@RequiredArgsConstructor
public class CLI {

  private final RecentBroadcastBusiness business;

  public void run(String[] args) {
    try {
      checkArgs(args);
      BusinessAction action = BusinessAction.getValue(args[0]);
      Channel channel = Channel
          .getByPublicName(args[1])
          .orElseThrow(() -> new UnknownChannelException(args[1]));
      business.handleRecentBroadcasts(action, channel);
    } catch (UnknownChannelException e) {
      log.error("Unknown channel: {}", args[1]);
    } catch (IllegalArgumentException e) {
      log.error("You must enter two arguments - action and channel.");
    } catch (Exception e) {
      log.error("Unexpected error.", e);
    }
  }

  private static void checkArgs(String[] args) {
    if (args.length != 2) {
      throw new IllegalArgumentException();
    }
  }

}
