package net.lecigne.somafm.history.application.services;

import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.lecigne.somafm.history.application.ports.in.SomaFmSongHistory;
import net.lecigne.somafm.history.application.ports.out.BroadcastRepository;
import net.lecigne.somafm.history.application.ports.out.SomaFmRepository;
import net.lecigne.somafm.history.domain.SomaFmCommand;
import net.lecigne.somafm.recentlib.Broadcast;
import net.lecigne.somafm.recentlib.Channel;

@Slf4j
public class DefaultSomaFmSongHistory implements SomaFmSongHistory {

  private final SomaFmRepository somaFmRepo;
  private final BroadcastRepository broadcastRepo;

  DefaultSomaFmSongHistory(SomaFmRepository somaFmRepo, BroadcastRepository broadcastRepo) {
    this.somaFmRepo = somaFmRepo;
    this.broadcastRepo = broadcastRepo;
  }

  @Override
  public List<Broadcast> run(SomaFmCommand command) {
    return switch (command.action()) {
      case DISPLAY -> fetchRecentBroadcasts(command.channel());
      case SAVE -> saveRecentBroadcasts(command.channel());
    };
  }

  private List<Broadcast> fetchRecentBroadcasts(Channel channel) {
    List<Broadcast> recentBroadcasts = somaFmRepo
        .getRecentBroadcasts(channel)
        .stream()
        .sorted(Comparator.comparing(Broadcast::time).reversed())
        .toList();
    log.info("Fetched {} last broadcasts from channel {}", recentBroadcasts.size(), channel.publicName());
    return recentBroadcasts;
  }

  private List<Broadcast> saveRecentBroadcasts(Channel channel) {
    List<Broadcast> recentBroadcasts = somaFmRepo.getRecentBroadcasts(channel);
    broadcastRepo.updateBroadcasts(recentBroadcasts);
    log.info("Saved {} last broadcasts from channel {}", recentBroadcasts.size(), channel.publicName());
    return recentBroadcasts;
  }

  public static SomaFmSongHistory init(BroadcastRepository broadcastRepo, SomaFmRepository somaFmRepo) {
    return new DefaultSomaFmSongHistory(somaFmRepo, broadcastRepo);
  }

}
