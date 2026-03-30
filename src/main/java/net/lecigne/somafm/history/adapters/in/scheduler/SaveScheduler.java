package net.lecigne.somafm.history.adapters.in.scheduler;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import net.lecigne.somafm.history.application.ports.in.SaveRecentBroadcastsUseCase;
import net.lecigne.somafm.history.bootstrap.config.SomaFmConfig.SchedulerConfig;
import net.lecigne.somafm.recentlib.Channel;
import net.lecigne.somafm.recentlib.PredefinedChannel;

@Slf4j
public class SaveScheduler {

  private final SaveRecentBroadcastsUseCase saveRecentBroadcastsUseCase;
  private final ScheduledExecutorService executorService;

  SaveScheduler(SaveRecentBroadcastsUseCase saveRecentBroadcastsUseCase, ScheduledExecutorService executorService) {
    this.saveRecentBroadcastsUseCase = saveRecentBroadcastsUseCase;
    this.executorService = executorService;
  }

  SaveScheduler(SaveRecentBroadcastsUseCase saveRecentBroadcastsUseCase) {
    this(saveRecentBroadcastsUseCase, Executors.newSingleThreadScheduledExecutor());
  }

  // TODO Remove duplicates
  public void schedule(List<Channel> channels, Duration period) {
    IntStream.range(0, channels.size()).forEach(i -> {
      long periodInSeconds = period.getSeconds();
      long delayInSeconds = periodInSeconds / channels.size();
      Channel channel = channels.get(i);
      Runnable runnable = () -> saveSafe(channel);
      long initialDelay = delayInSeconds * i;
      executorService.scheduleAtFixedRate(runnable, initialDelay, periodInSeconds, TimeUnit.SECONDS);
      log.info("Scheduled save for channel {} every {} seconds with delay {}", channel.publicName(), periodInSeconds, initialDelay);
    });
  }

  private void saveSafe(Channel channel) {
    try {
      saveRecentBroadcastsUseCase.saveRecent(channel);
    } catch (Exception e) {
      log.error("Error while running scheduled save for channel {}", channel.publicName(), e);
    }
  }

  public void shutdown() {
    executorService.shutdown();
    try {
      if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
        executorService.shutdownNow();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      executorService.shutdownNow();
    }
  }

  public static void init(SaveRecentBroadcastsUseCase saveRecentBroadcastsUseCase, SchedulerConfig schedulerConfig) {
    List<Channel> channels = schedulerConfig
        .getChannels()
        .stream()
        .map(SaveScheduler::mapToChannel)
        .toList();
    var saveScheduler = new SaveScheduler(saveRecentBroadcastsUseCase);
    Runtime.getRuntime().addShutdownHook(new Thread(saveScheduler::shutdown, "save-scheduler-shutdown"));
    saveScheduler.schedule(channels, schedulerConfig.getPeriod());
  }

  private static Channel mapToChannel(String configuredName) {
    return PredefinedChannel
        .getByInternalName(configuredName)
        .orElseThrow(() -> new IllegalArgumentException("Unknown scheduler channel: " + configuredName));
  }

}
