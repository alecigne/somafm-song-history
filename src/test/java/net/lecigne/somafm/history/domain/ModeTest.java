package net.lecigne.somafm.history.domain;

import static org.assertj.core.api.Assertions.assertThat;

import net.lecigne.somafm.history.domain.model.Mode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("An action")
class ModeTest {

  @Test
  void should_default_to_display_when_wrong_action() {
    Mode mode = Mode.getValue("foobar");
    assertThat(mode).isEqualTo(Mode.DISPLAY);
  }

}
