package net.lecigne.somafm.cli;

import static net.lecigne.somafm.business.BusinessAction.DISPLAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

import net.lecigne.somafm.business.BusinessAction;
import net.lecigne.somafm.business.RecentBroadcastBusiness;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

@DisplayName("The CLI")
class CLITest {

  private RecentBroadcastBusiness business;
  private CLI cli;

  @BeforeEach
  void setUp() {
    business = Mockito.mock(RecentBroadcastBusiness.class);
    cli = new CLI(business);
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
  void should_use_display_action_if_wrong_action() {
    // Given
    var args = new String[]{"foobar", "Drone Zone"};
    ArgumentCaptor<BusinessAction> argumentCaptor = ArgumentCaptor.forClass(BusinessAction.class);

    // When
    cli.run(args);

    // Then
    Mockito.verify(business).handleRecentBroadcasts(argumentCaptor.capture(), any());
    BusinessAction action = argumentCaptor.getValue();
    assertThat(action).isEqualTo(DISPLAY);
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

}
