package net.lecigne.somafm.utils;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TimeUtils {

  /**
   * Convert the local broadcasting time to an instant.
   * <p>
   * Example:
   * <ul>
   *   <li>A snapshot is taken on 2022-10-23 11:00 UTC</li>
   *   <li>The local snapshot time in San Francisco will be 2022-10-23 04:00 UTC-7</li>
   *   <li>The local broadcast time is 22:25</li>
   *   <li>We build a candidate local snapshot time: 2022-10-23 22:25 UTC-7</li>
   *   <li>This date is AFTER the local snapshot time, so it must be the day before (only two days are represented in
   *   the recent broadcast list)</li>
   * </ul>
   */
  public static Instant localBroadcastTimeToInstant(LocalTime localBroadcastTime, Instant snapshotTime,
      ZoneId broadcastLocation) {
    ZonedDateTime snapshotDateTimeAtLocation = snapshotTime.atZone(broadcastLocation);
    ZonedDateTime localDateTimeSameDayAsSnapshot = localBroadcastTime
        .atDate(snapshotDateTimeAtLocation.toLocalDate())
        .atZone(broadcastLocation);
    if (localDateTimeSameDayAsSnapshot.isBefore(snapshotDateTimeAtLocation)) {
      return localDateTimeSameDayAsSnapshot.toInstant();
    } else {
      return localDateTimeSameDayAsSnapshot.minus(1, ChronoUnit.DAYS).toInstant();
    }
  }

}
