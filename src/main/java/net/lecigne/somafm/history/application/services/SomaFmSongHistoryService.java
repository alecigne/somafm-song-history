package net.lecigne.somafm.history.application.services;

import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.lecigne.somafm.history.application.ports.in.FetchRecentBroadcastsUseCase;
import net.lecigne.somafm.history.application.ports.in.GetBroadcastsUseCase;
import net.lecigne.somafm.history.application.ports.in.RunCommandUseCase;
import net.lecigne.somafm.history.application.ports.out.BroadcastRepository;
import net.lecigne.somafm.history.application.ports.out.SomaFmRepository;
import net.lecigne.somafm.history.domain.model.Page;
import net.lecigne.somafm.history.domain.model.SomaFmCommand;
import net.lecigne.somafm.history.domain.services.PageRequestValidator;
import net.lecigne.somafm.recentlib.Broadcast;
import net.lecigne.somafm.recentlib.Channel;

@Slf4j
public class SomaFmSongHistoryService implements RunCommandUseCase, GetBroadcastsUseCase, FetchRecentBroadcastsUseCase {

  private final SomaFmRepository somaFmRepo;
  private final BroadcastRepository broadcastRepo;
  private final PageRequestValidator pageRequestValidator;

  SomaFmSongHistoryService(SomaFmRepository somaFmRepo, BroadcastRepository broadcastRepo) {
    this(somaFmRepo, broadcastRepo, PageRequestValidator.defaultValidator());
  }

  SomaFmSongHistoryService(
      SomaFmRepository somaFmRepo,
      BroadcastRepository broadcastRepo,
      PageRequestValidator pageRequestValidator) {
    this.somaFmRepo = somaFmRepo;
    this.broadcastRepo = broadcastRepo;
    this.pageRequestValidator = pageRequestValidator;
  }

  @Override
  public List<Broadcast> run(SomaFmCommand command) {
    return switch (command.mode()) {
      case DISPLAY -> fetchRecent(command.channel());
      case SAVE -> saveRecentBroadcasts(command.channel());
    };
  }

  private List<Broadcast> saveRecentBroadcasts(Channel channel) {
    List<Broadcast> recentBroadcasts = somaFmRepo.fetchRecentBroadcasts(channel);
    broadcastRepo.updateBroadcasts(recentBroadcasts);
    log.info("Saved {} last broadcasts from channel {}", recentBroadcasts.size(), channel.publicName());
    return recentBroadcasts;
  }

  @Override
  public Page<Broadcast> getBroadcasts(int page, int size) {
    pageRequestValidator.validate(page, size);
    long totalElements = broadcastRepo.countBroadcasts();
    long totalPagesLong = totalElements == 0 ? 0 : ((totalElements - 1) / size) + 1;
    int totalPages = (int) Math.min(totalPagesLong, Integer.MAX_VALUE);
    List<Broadcast> items = broadcastRepo.getBroadcasts(page, size);
    return new Page<>(page, size, totalElements, totalPages, items);
  }

  public static SomaFmSongHistoryService init(BroadcastRepository broadcastRepo, SomaFmRepository somaFmRepo) {
    return new SomaFmSongHistoryService(somaFmRepo, broadcastRepo);
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

}
