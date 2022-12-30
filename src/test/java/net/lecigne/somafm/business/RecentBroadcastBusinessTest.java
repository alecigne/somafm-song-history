package net.lecigne.somafm.business;

import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemOut;
import static net.lecigne.somafm.business.BusinessAction.DISPLAY;
import static net.lecigne.somafm.fixtures.TestFixtures.breakSongFixture;
import static net.lecigne.somafm.fixtures.TestFixtures.dirkSerriesSongFixture;
import static net.lecigne.somafm.fixtures.TestFixtures.igneousFlameSongFixture;
import static net.lecigne.somafm.model.Channel.DRONE_ZONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.github.stefanbirkner.systemlambda.Statement;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Set;
import net.lecigne.somafm.exception.SomaFmHtmlParsingException;
import net.lecigne.somafm.mappers.DisplayedBroadcastMapper;
import net.lecigne.somafm.model.Broadcast;
import net.lecigne.somafm.repository.BroadcastRepository;
import net.lecigne.somafm.repository.DefaultBroadcastRepository;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@DisplayName("The broadcast business")
class RecentBroadcastBusinessTest {

  private final DisplayedBroadcastMapper mapper = new DisplayedBroadcastMapper(ZoneId.of("Europe/Paris"));

  @Nested
  class when_displaying_broadcasts {

    @Test
    void should_display_a_list_of_recent_broadcasts() throws Exception {
      // Given
      BroadcastRepository repository = Mockito.mock(DefaultBroadcastRepository.class);
      given(repository.getRecentBroadcasts(any())).willReturn(Set.of(
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
              .build()));
      var business = new RecentBroadcastBusiness(repository, mapper);
      var expected = """
          [2021-01-01 14:20:00 @ Drone Zone] Break / Station ID
          [2021-01-01 14:15:00 @ Drone Zone] Igneous Flame - Incandescent Arc
          [2021-01-01 14:00:00 @ Drone Zone] Dirk Serries' Microphonics - VI"""
          .replaceAll("[\\r\\n]", "");

      // When
      Statement statement = () -> business.handleRecentBroadcasts(DISPLAY, DRONE_ZONE);
      String actual = tapSystemOut(statement).replaceAll("[\\r\\n]", "");

      // Then
      assertThat(actual).isEqualTo(expected);
    }

    @Test
    void should_display_an_informative_message_if_broadcast_retrieval_fails() throws Exception {
      // Given
      BroadcastRepository repository = Mockito.mock(DefaultBroadcastRepository.class);
      given(repository.getRecentBroadcasts(any())).willThrow(IOException.class);
      var business = new RecentBroadcastBusiness(repository, mapper);
      var expected = "Unable to get recent broadcasts from SomaFM at this time. Please try again later.";

      try (LogCaptor logCaptor = LogCaptor.forClass(RecentBroadcastBusiness.class)) {
        // When
        business.handleRecentBroadcasts(DISPLAY, DRONE_ZONE);

        // Then
        assertThat(logCaptor.getErrorLogs()).containsExactly(expected);
      }
    }

    @Test
    void should_display_an_informative_message_if_parsing_fails() throws Exception {
      // Given
      BroadcastRepository repository = Mockito.mock(DefaultBroadcastRepository.class);
      var expected = "Unable to parse SomaFM recent broadcasts page.";
      given(repository.getRecentBroadcasts(any())).willThrow(new SomaFmHtmlParsingException(expected));
      var business = new RecentBroadcastBusiness(repository, mapper);

      try (LogCaptor logCaptor = LogCaptor.forClass(RecentBroadcastBusiness.class)) {
        // When
        business.handleRecentBroadcasts(DISPLAY, DRONE_ZONE);

        // Then
        assertThat(logCaptor.getErrorLogs()).containsExactly(expected);
      }
    }

  }

}
