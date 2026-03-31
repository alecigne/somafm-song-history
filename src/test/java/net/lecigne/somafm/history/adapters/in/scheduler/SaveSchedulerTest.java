package net.lecigne.somafm.history.adapters.in.scheduler;

import static net.lecigne.somafm.recentlib.PredefinedChannel.DEEP_SPACE_ONE;
import static net.lecigne.somafm.recentlib.PredefinedChannel.DRONE_ZONE;
import static net.lecigne.somafm.recentlib.PredefinedChannel.GROOVE_SALAD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.lecigne.somafm.history.application.ports.in.SaveRecentBroadcastsUseCase;
import net.lecigne.somafm.recentlib.Broadcast;
import net.lecigne.somafm.recentlib.Channel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

@DisplayName("The save scheduler")
class SaveSchedulerTest {

  @Test
  void should_schedule_saving_of_target_channels() {
    // Given
    var testService = new TestService();
    var executorService = Mockito.mock(ScheduledExecutorService.class);
    var saveScheduler = new SaveScheduler(testService, executorService);

    List<Channel> channels = List.of(DRONE_ZONE, GROOVE_SALAD, DEEP_SPACE_ONE);
    var period = Duration.ofMinutes(10);

    // When
    saveScheduler.schedule(channels, period);

    // Then
    ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
    ArgumentCaptor<Long> initialDelayCaptor = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<Long> periodCaptor = ArgumentCaptor.forClass(Long.class);
    verify(executorService, times(3)).scheduleAtFixedRate(
        runnableCaptor.capture(),
        initialDelayCaptor.capture(),
        periodCaptor.capture(),
        eq(TimeUnit.SECONDS));
    runnableCaptor.getAllValues().forEach(Runnable::run);
    assertThat(testService.targetChannels).containsExactly(DRONE_ZONE, GROOVE_SALAD, DEEP_SPACE_ONE);
    assertThat(initialDelayCaptor.getAllValues()).containsExactly(0L, 200L, 400L);
    assertThat(periodCaptor.getAllValues()).containsOnly(600L);
  }

  static class TestService implements SaveRecentBroadcastsUseCase {

    List<Channel> targetChannels = new ArrayList<>();

    @Override
    public List<Broadcast> saveRecent(Channel channel) {
      targetChannels.add(channel);
      return List.of();
    }

  }

}
