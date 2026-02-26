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

  public static final String ROOT_CONFIG = "config";

  private String userAgent;
  private String timezone;
  @Optional
  private DbConfig db;
  @Optional
  private SchedulerConfig scheduler;

  @NoArgsConstructor
  @Getter
  @Setter
  public static class DbConfig {
    private String url;
    private String user;
    private String password;
  }

  @NoArgsConstructor
  @Getter
  @Setter
  public static class SchedulerConfig {
    private boolean enabled;
    private Duration period;
    private List<String> channels;
  }

  public boolean isDbActivated() {
    return Objects.nonNull(db) && Stream.of(db.getUrl(), db.getUser(), db.getPassword()).allMatch(Objects::nonNull);
  }

  public boolean isSchedulerActivated() {
    return Objects.nonNull(scheduler)
        && scheduler.isEnabled()
        && scheduler.getPeriod().getSeconds() > 0
        && Objects.nonNull(scheduler.getChannels())
        && !scheduler.getChannels().isEmpty();
  }

}
