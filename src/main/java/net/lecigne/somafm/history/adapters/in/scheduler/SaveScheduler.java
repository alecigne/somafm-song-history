package net.lecigne.somafm.history.adapters.in.scheduler;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import net.lecigne.somafm.history.application.ports.in.SaveRecentBroadcastsUseCase;
import net.lecigne.somafm.recentlib.Channel;

@Slf4j
public class SaveScheduler {

  private final SaveRecentBroadcastsUseCase saveRecentBroadcastsUseCase;
  private final Duration period;
  private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

  SaveScheduler(SaveRecentBroadcastsUseCase saveRecentBroadcastsUseCase, Duration period) {
    this.saveRecentBroadcastsUseCase = saveRecentBroadcastsUseCase;
    this.period = period;
  }

  public void schedule(List<Channel> channels) {
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

  public static SaveScheduler init(SaveRecentBroadcastsUseCase saveRecentBroadcastsUseCase, Duration period) {
    return new SaveScheduler(saveRecentBroadcastsUseCase, period);
  }

}
