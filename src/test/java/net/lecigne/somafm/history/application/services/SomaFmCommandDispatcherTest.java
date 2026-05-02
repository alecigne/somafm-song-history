package net.lecigne.somafm.history.application.services;

import static net.lecigne.somafm.history.fixtures.Fxt.dirkSerriesSix;
import static net.lecigne.somafm.recentlib.PredefinedChannel.DRONE_ZONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import net.lecigne.somafm.history.application.ports.in.FetchRecentBroadcastsUseCase;
import net.lecigne.somafm.history.application.ports.in.SaveRecentBroadcastsUseCase;
import net.lecigne.somafm.history.domain.model.Mode;
import net.lecigne.somafm.history.domain.model.SomaFmCommand;
import net.lecigne.somafm.recentlib.Broadcast;
import net.lecigne.somafm.recentlib.Channel;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The SomaFM command dispatcher")
class SomaFmCommandDispatcherTest {

  private final FakeFetchRecentBroadcastsUseCase fetchRecentBroadcastsUseCase = new FakeFetchRecentBroadcastsUseCase();
  private final FakeSaveRecentBroadcastsUseCase saveRecentBroadcastsUseCase = new FakeSaveRecentBroadcastsUseCase();
  private final SomaFmCommandDispatcher dispatcher = new SomaFmCommandDispatcher(fetchRecentBroadcastsUseCase, saveRecentBroadcastsUseCase);

  @AfterEach
  void tearDown() {
    fetchRecentBroadcastsUseCase.reset();
    saveRecentBroadcastsUseCase.reset();
  }

  @Test
  void should_dispatch_display_command_to_recent_fetch() {
    // Given
    Broadcast broadcast = broadcastAt("2021-01-01T11:00:00.00Z");
    fetchRecentBroadcastsUseCase.broadcasts = List.of(broadcast);
    var command = new SomaFmCommand(Mode.DISPLAY, DRONE_ZONE);

    // When
    List<Broadcast> result = dispatcher.runCommand(command);

    // Then
    assertThat(result).containsExactly(broadcast);
    assertThat(fetchRecentBroadcastsUseCase.receivedChannel).isEqualTo(DRONE_ZONE);
  }

  @Test
  void should_dispatch_save_command_to_recent_save() {
    // Given
    Broadcast broadcast = broadcastAt("2021-01-01T13:00:00.00Z");
    saveRecentBroadcastsUseCase.broadcasts = List.of(broadcast);
    var command = new SomaFmCommand(Mode.SAVE, DRONE_ZONE);

    // When
    List<Broadcast> result = dispatcher.runCommand(command);

    // Then
    assertThat(result).containsExactly(broadcast);
    assertThat(saveRecentBroadcastsUseCase.receivedChannel).isEqualTo(DRONE_ZONE);
  }

  @Test
  void should_reject_api_command() {
    // Given
    var command = new SomaFmCommand(Mode.API, DRONE_ZONE);

    // "When"
    ThrowingCallable call = () -> dispatcher.runCommand(command);

    // When / Then
    assertThatThrownBy(call).isInstanceOf(IllegalArgumentException.class);
  }

  private static final class FakeFetchRecentBroadcastsUseCase implements FetchRecentBroadcastsUseCase {

    private Channel receivedChannel;
    private List<Broadcast> broadcasts = List.of();

    @Override
    public List<Broadcast> fetchRecent(Channel channel) {
      receivedChannel = channel;
      return broadcasts;
    }

    private void reset() {
      receivedChannel = null;
      broadcasts = List.of();
    }

  }

  private static final class FakeSaveRecentBroadcastsUseCase implements SaveRecentBroadcastsUseCase {

    private Channel receivedChannel;
    private List<Broadcast> broadcasts = List.of();

    @Override
    public List<Broadcast> saveRecent(Channel channel) {
      receivedChannel = channel;
      return broadcasts;
    }

    private void reset() {
      receivedChannel = null;
      broadcasts = List.of();
    }

  }

  private static Broadcast broadcastAt(String time) {
    return Broadcast.builder()
        .time(Instant.parse(time))
        .channel(DRONE_ZONE)
        .song(dirkSerriesSix())
        .build();
  }

}
