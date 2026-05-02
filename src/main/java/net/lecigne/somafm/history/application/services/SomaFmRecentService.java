package net.lecigne.somafm.history.application.services;

import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.lecigne.somafm.history.application.ports.in.FetchRecentBroadcastsUseCase;
import net.lecigne.somafm.history.application.ports.in.SaveRecentBroadcastsUseCase;
import net.lecigne.somafm.history.application.ports.out.BroadcastRepository;
import net.lecigne.somafm.history.application.ports.out.SomaFmRepository;
import net.lecigne.somafm.recentlib.Broadcast;
import net.lecigne.somafm.recentlib.Channel;

@Slf4j
public class SomaFmRecentService implements FetchRecentBroadcastsUseCase, SaveRecentBroadcastsUseCase {

  private final SomaFmRepository somaFmRepo;
  private final BroadcastRepository broadcastRepo;

  SomaFmRecentService(SomaFmRepository somaFmRepo, BroadcastRepository broadcastRepo) {
    this.somaFmRepo = somaFmRepo;
    this.broadcastRepo = broadcastRepo;
  }

  @Override
  public List<Broadcast> saveRecent(Channel channel) {
    List<Broadcast> recentBroadcasts = somaFmRepo.fetchRecentBroadcasts(channel);
    broadcastRepo.updateBroadcasts(recentBroadcasts);
    log.info("Saved {} last broadcasts from channel {}", recentBroadcasts.size(), channel.publicName());
    return recentBroadcasts;
  }

  @Override
  public List<Broadcast> fetchRecent(Channel channel) {
    List<Broadcast> recentBroadcasts = somaFmRepo
        .fetchRecentBroadcasts(channel)
        .stream()
        .sorted(Comparator.comparing(Broadcast::time).reversed())
        .toList();
    log.info("Fetched {} last broadcasts from channel {}", recentBroadcasts.size(), channel.publicName());
    return recentBroadcasts;
  }

  public static SomaFmRecentService init(SomaFmRepository somaFmRepo, BroadcastRepository broadcastRepo) {
    return new SomaFmRecentService(somaFmRepo, broadcastRepo);
  }

}
