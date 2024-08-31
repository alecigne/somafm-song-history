package net.lecigne.somafm.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("The time utility class")
class TimeUtilsTest {

  @ParameterizedTest
  @MethodSource("dataProvider")
  void should_work(Instant snapshotTimeUtc, ZoneId broadcastLocation, LocalTime localBroadcastTime,
      Instant expectedUtc) {
    // When
    Instant utc = TimeUtils.localBroadcastTimeToInstant(localBroadcastTime, snapshotTimeUtc, broadcastLocation);

    // Then
    assertThat(utc).isEqualTo(expectedUtc);
  }

  /**
   * A track snapshotted on [1] on a radio located in [2] and displaying a local broadcasting time of [3] was played on
   * [4] (UTC time).
   */
  private static Stream<Arguments> dataProvider() {
    return Stream.of(
        Arguments.of("2022-06-26T17:00:00Z", "Asia/Tokyo", "01:30", "2022-06-26T16:30:00Z"),
        Arguments.of("2022-06-26T12:00:00Z", "America/Los_Angeles", "03:30", "2022-06-26T10:30:00Z"),
        Arguments.of("2022-03-13T11:00:00Z", "America/Los_Angeles", "01:00", "2022-03-13T09:00:00Z"),
        Arguments.of("2022-06-26T12:00:00Z", "America/Los_Angeles", "23:30", "2022-06-26T06:30:00Z"),

        // Transition from daylight saving time to standard time
        // Since (1) there will be "another 1 AM" and (2) the "first 1 AM" is always chosen when converting a local time
        // on the day DST ends, there will be 2 hours between a track played at 01:15 AM and another played at 02:15 AM
        Arguments.of("2021-11-07T08:30:00Z", "America/Los_Angeles", "01:15", "2021-11-07T08:15:00Z"),
        Arguments.of("2021-11-07T10:30:00Z", "America/Los_Angeles", "02:15", "2021-11-07T10:15:00Z")
    );
  }

}
