package net.lecigne.somafm.adapters.primary;

import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemOut;
import static net.lecigne.somafm.fixtures.TestFixtures.breakSongFixture;
import static net.lecigne.somafm.fixtures.TestFixtures.dirkSerriesSongFixture;
import static net.lecigne.somafm.fixtures.TestFixtures.igneousFlameSongFixture;
import static net.lecigne.somafm.recentlib.PredefinedChannel.DRONE_ZONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.github.stefanbirkner.systemlambda.Statement;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Stream;
import net.lecigne.somafm.application.api.SomaFmSongHistoryApi;
import net.lecigne.somafm.recentlib.Broadcast;
import net.lecigne.somafm.recentlib.SomaFmException;
import nl.altindag.log.LogCaptor;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

@DisplayName("The CLI")
class CLITest {

  private SomaFmSongHistoryApi api;
  // Things are easier with the real mapper
  private final DisplayedBroadcastMapper mapper = new DisplayedBroadcastMapper(ZoneId.of("Europe/Paris"));
  private CLI cli;

  @BeforeEach
  void setUp() {
    api = Mockito.mock(SomaFmSongHistoryApi.class);
    cli = new CLI(api, mapper);
  }

  @AfterEach
  void tearDown() {
    Mockito.reset(api);
  }

  @Test
  void should_log_error_if_wrong_number_of_args() {
    // Given
    var args = new String[]{"one arg"};
    var expected = "You must enter two arguments - action and channel.";

    try (LogCaptor logCaptor = LogCaptor.forClass(CLI.class)) {
      // When
      cli.run(args);

      // Then
      assertThat(logCaptor.getErrorLogs()).contains(expected);
    }
  }

  @Test
  void should_log_if_unknown_channel() {
    // Given
    var args = new String[]{"display", "Foobar FM"};
    var expected = "Unknown channel: Foobar FM";

    try (LogCaptor logCaptor = LogCaptor.forClass(CLI.class)) {
      // When
      cli.run(args);

      // Then
      assertThat(logCaptor.getErrorLogs()).contains(expected);
    }
  }

  @Test
  void should_log_if_broadcast_retrieval_fails() {
    // Given
    given(api.fetchRecentBroadcasts(any())).willThrow(SomaFmException.class);
    var expected = "Error while fetching broadcasts.";

    try (LogCaptor logCaptor = LogCaptor.forClass(CLI.class)) {
      // When
      cli.run(new String[]{"display", "Drone Zone"});

      // Then
      assertThat(logCaptor.getErrorLogs()).containsExactly(expected);
    }
  }

  @ParameterizedTest
  @MethodSource
  void should_fetch_and_display(String action) throws Exception {
    // Given
    var args = new String[]{action, "Drone Zone"};
    given(api.fetchRecentBroadcasts(any())).willReturn(Fixtures.getRecentBroadcasts());
    var expected = Fixtures.getDisplayedRecentBroadcasts().replaceAll("[\\r\\n]", "");

    // When
    Statement statement = () -> cli.run(args);
    String actual = tapSystemOut(statement).replaceAll("[\\r\\n]", "");

    // Then
    verify(api, times(1)).fetchRecentBroadcasts(any());
    verify(api, never()).saveRecentBroadcasts(any());
    assertThat(actual).contains(expected);
  }


  static Stream<Arguments> should_fetch_and_display() {
    return Stream.of(
        arguments("display"), arguments("unknown_action")
    );
  }

  @Test
  void should_save_and_display() throws Exception {
    // Given
    var args = new String[]{"save", "Drone Zone"};
    given(api.saveRecentBroadcasts(any())).willReturn(Fixtures.getRecentBroadcasts());
    var expected = Fixtures.getDisplayedRecentBroadcasts().replaceAll("[\\r\\n]", "");

    // When
    Statement statement = () -> cli.run(args);
    String actual = tapSystemOut(statement).replaceAll("[\\r\\n]", "");

    // Then
    verify(api, never()).fetchRecentBroadcasts(any());
    verify(api, times(1)).saveRecentBroadcasts(any());
    assertThat(actual).contains(expected);
  }

  static class Fixtures {

    private static @NotNull List<Broadcast> getRecentBroadcasts() {
      return List.of(
          Broadcast.builder()
              .time(Instant.parse("2021-01-01T13:00:00.00Z"))
              .channel(DRONE_ZONE)
              .song(dirkSerriesSongFixture())
              .build(),
          Broadcast.builder()
              .time(Instant.parse("2021-01-01T13:15:00.00Z"))
              .channel(DRONE_ZONE)
              .song(igneousFlameSongFixture())
              .build(),
          Broadcast.builder()
              .time(Instant.parse("2021-01-01T13:20:00.00Z"))
              .channel(DRONE_ZONE)
              .song(breakSongFixture())
              .build());
    }

    private static @NotNull String getDisplayedRecentBroadcasts() {
      return """
          [2021-01-01 14:20:00 @ Drone Zone] Break / Station ID
          [2021-01-01 14:15:00 @ Drone Zone] Igneous Flame - Incandescent Arc
          [2021-01-01 14:00:00 @ Drone Zone] Dirk Serries' Microphonics - VI""";
    }

  }

}
