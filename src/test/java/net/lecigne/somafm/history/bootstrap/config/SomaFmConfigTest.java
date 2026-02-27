package net.lecigne.somafm.history.bootstrap.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;
import net.lecigne.somafm.history.bootstrap.config.SomaFmConfig.DbConfig;
import net.lecigne.somafm.history.bootstrap.config.SomaFmConfig.SchedulerConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("The application config")
class SomaFmConfigTest {

  @ParameterizedTest
  @MethodSource("configData")
  void should_detect_db_activation(
      // Given
      SomaFmConfig config, boolean expected
  ) {
    // When
    boolean isDbActivated = config.isDbConfigured();

    // Then
    assertThat(isDbActivated).isEqualTo(expected);
  }

  @ParameterizedTest
  @MethodSource("schedulerData")
  void should_detect_scheduler_activation(
      // Given
      SomaFmConfig config, boolean expected
  ) {
    // When
    boolean isSchedulerActivated = config.isSchedulerConfigured();

    // Then
    assertThat(isSchedulerActivated).isEqualTo(expected);
  }

  public static Stream<Arguments> configData() {
    // Nothing
    var noDb = new SomaFmConfig();
    // Db, no fields
    var dbNoFields = new SomaFmConfig();
    dbNoFields.setDb(new DbConfig());
    // Db, some fields
    var dbSomeFields = buildConfig(null, "user", null);
    // Db, all fields
    var dbAllFields = buildConfig("url", "user", "password");
    return Stream.of(
        arguments(noDb, false),
        arguments(dbNoFields, false),
        arguments(dbSomeFields, false),
        arguments(dbAllFields, true)
    );
  }

  public static Stream<Arguments> schedulerData() {
    // Nothing
    var noScheduler = new SomaFmConfig();
    // Scheduler, disabled
    var schedulerDisabled = buildSchedulerConfig(false, Duration.ofMinutes(10), List.of("Drone Zone"));
    // Scheduler, invalid period
    var schedulerInvalidPeriod = buildSchedulerConfig(true, Duration.ofMinutes(0), List.of("Drone Zone"));
    // Scheduler, no channels
    var schedulerNoChannels = buildSchedulerConfig(true, Duration.ofMinutes(10), List.of());
    // Scheduler, valid
    var schedulerValid = buildSchedulerConfig(true, Duration.ofMinutes(10), List.of("Drone Zone"));
    return Stream.of(
        arguments(noScheduler, false),
        arguments(schedulerDisabled, false),
        arguments(schedulerInvalidPeriod, false),
        arguments(schedulerNoChannels, false),
        arguments(schedulerValid, true)
    );
  }

  @SuppressWarnings("SameParameterValue")
  private static SomaFmConfig buildConfig(String url, String user, String password) {
    var dbConfig = new DbConfig();
    dbConfig.setUrl(url);
    dbConfig.setUser(user);
    dbConfig.setPassword(password);
    var config = new SomaFmConfig();
    config.setDb(dbConfig);
    return config;
  }

  private static SomaFmConfig buildSchedulerConfig(boolean enabled, Duration period, List<String> channels) {
    var schedulerConfig = new SchedulerConfig();
    schedulerConfig.setEnabled(enabled);
    schedulerConfig.setPeriod(period);
    schedulerConfig.setChannels(channels);
    var config = new SomaFmConfig();
    config.setSchedulerConfig(schedulerConfig);
    return config;
  }

}
