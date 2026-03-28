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
  @Optional
  private DbConfig dbConfig;
  @Optional
  private ServerConfig server;
  @Optional
  private SchedulerConfig schedulerConfig;

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
  public static class ServerConfig {
    private int port;
  }

  @NoArgsConstructor
  @Getter
  @Setter
  public static class SchedulerConfig {
    private boolean enabled;
    private Duration period;
    private List<String> channels;
  }

  public boolean isDbConfigured() {
    return Objects.nonNull(dbConfig) && Stream.of(dbConfig.getUrl(), dbConfig.getUser(), dbConfig.getPassword()).allMatch(Objects::nonNull);
  }

  public boolean isServerConfigured() {
    return Objects.nonNull(server) && server.getPort() > 0;
  }

  public boolean isSchedulerConfigured() {
    return Objects.nonNull(schedulerConfig)
        && schedulerConfig.isEnabled()
        && schedulerConfig.getPeriod().getSeconds() > 0
        && Objects.nonNull(schedulerConfig.getChannels())
        && !schedulerConfig.getChannels().isEmpty();
  }

  /* Getters and setters for config names */

  @Optional
  public DbConfig getDb() {
    return dbConfig;
  }

  public void setDb(DbConfig db) {
    this.dbConfig = db;
  }

  @Optional
  public SchedulerConfig getScheduler() {
    return schedulerConfig;
  }

  public void setScheduler(SchedulerConfig scheduler) {
    this.schedulerConfig = scheduler;
  }

}
