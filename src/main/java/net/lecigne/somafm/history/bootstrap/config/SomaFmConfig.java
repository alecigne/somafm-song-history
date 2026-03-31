package net.lecigne.somafm.history.bootstrap.config;

import com.typesafe.config.Optional;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class SomaFmConfig {

  private String userAgent;
  private String timezone;

  /**
   * Database configuration.
   * <p>
   * It is optional during serialization, but might cause a semantic error if not present during
   * loading depending on the target mode.
   */
  @Optional
  private DbConfig db;

  /**
   * API configuration.
   * <p>
   * It is optional during serialization, but might cause a semantic error if not present during
   * loading depending on the target mode.
   */
  @Optional
  private ApiConfig api;

  @NoArgsConstructor
  @Getter
  @Setter
  public static class DbConfig {
    private String url;
    private String user;
    private String password;

    public boolean isOk() {
      return Stream.of(url, user, password).allMatch(Objects::nonNull);
    }

  }

  @NoArgsConstructor
  @Getter
  @Setter
  public static class ApiConfig {

    private int port;
    private SchedulerConfig scheduler;

    public boolean isSchedulerEnabled() {
      return scheduler.isEnabled();
    }

    public boolean isOk() {
      return port > 0 && isSchedulerConfigOk();
    }

    private boolean isSchedulerConfigOk() {
      if (!scheduler.isEnabled()) return true;
      return scheduler.getPeriod().getSeconds() > 0 && Objects.nonNull(scheduler.getChannels()) && !scheduler.getChannels().isEmpty();
    }

  }

  @NoArgsConstructor
  @Getter
  @Setter
  public static class SchedulerConfig {
    private boolean enabled;
    @Optional
    private Duration period;
    @Optional
    private List<String> channels;
  }

}
