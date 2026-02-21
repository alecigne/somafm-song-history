package net.lecigne.somafm.history.domain;

import static org.assertj.core.api.Assertions.assertThat;

import net.lecigne.somafm.history.domain.Action;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("An action")
class ActionTest {

  @Test
  void should_default_to_display_when_wrong_action() {
    Action action = Action.getValue("foobar");
    assertThat(action).isEqualTo(Action.DISPLAY);
  }

}
