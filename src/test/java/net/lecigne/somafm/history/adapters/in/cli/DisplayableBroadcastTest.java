package net.lecigne.somafm.history.adapters.in.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("A displayable broadcast")
class DisplayableBroadcastTest {

  @ParameterizedTest
  @MethodSource
  void should_build_a_correct_string(DisplayableBroadcast displayableBroadcast, String expectedString) {
    String actualString = displayableBroadcast.toString();
    assertThat(actualString).isEqualTo(expectedString);
  }

  private static Stream<Arguments> should_build_a_correct_string() {
    return Stream.of(
        argumentSet("Full broadcast",
            DisplayableBroadcast.builder()
                .time("2025-02-16 16:00:00")
                .channel("Groove Salad")
                .artist("an_artist")
                .title("a_title")
                .build(),
            "[2025-02-16 16:00:00 @ Groove Salad] an_artist - a_title"),
        argumentSet("Broadcast without artist",
            DisplayableBroadcast.builder()
                .time("2025-02-16 16:00:00")
                .channel("Groove Salad")
                .title("a_title")
                .build(),
            "[2025-02-16 16:00:00 @ Groove Salad] a_title")
    );
  }

}
