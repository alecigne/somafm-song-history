package net.lecigne.somafm.history.bootstrap.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import java.time.Duration;
import java.util.List;
import net.lecigne.somafm.history.bootstrap.config.SomaFmConfig.DbConfig;
import net.lecigne.somafm.history.bootstrap.config.SomaFmConfig.SchedulerConfig;
import net.lecigne.somafm.history.bootstrap.config.SomaFmConfig.ApiConfig;
import net.lecigne.somafm.history.domain.model.Mode;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("The config loader")
@SuppressWarnings("SameParameterValue")
class ConfigLoaderTest {

  @Nested
  class when_called_in_display_mode {

    @Test
    void should_load_minimal_config() {
      // Given
      String minimalConfig = """
          userAgent = "ua"
          timezone = "Europe/Paris"
          """;
      SomaFmConfig expectedSomaFmConfig = expectedConfig("ua", "Europe/Paris", null, null);

      // When
      SomaFmConfig somaFmConfig = ConfigLoader.loadForMode(ConfigFactory.parseString(minimalConfig), Mode.DISPLAY);

      // Then
      assertThat(somaFmConfig).usingRecursiveComparison().isEqualTo(expectedSomaFmConfig);
    }

    @Test
    void should_load_optional_sections() {
      // Given
      String fullConfig = """
          userAgent = "ua"
          timezone = "Europe/Paris"
          db {
            url = "jdbc:postgresql://localhost:5432/somafm"
            user = "somafm"
            password = "password"
          }
          api {
            port = 7070
            scheduler {
              enabled = true
              period = "10m"
              channels = ["dronezone"]
            }
          }
          """;
      SomaFmConfig expectedSomaFmConfig = expectedConfig(
          "ua",
          "Europe/Paris",
          dbConfig("jdbc:postgresql://localhost:5432/somafm", "somafm", "password"),
          apiConfig(7070, true, Duration.ofMinutes(10), List.of("dronezone")));

      // When
      SomaFmConfig somaFmConfig = ConfigLoader.loadForMode(ConfigFactory.parseString(fullConfig), Mode.DISPLAY);

      // Then
      assertThat(somaFmConfig).usingRecursiveComparison().isEqualTo(expectedSomaFmConfig);
    }
  }

  @Nested
  class when_called_in_save_mode {

    @Test
    void should_load_with_db_config() {
      // Given
      String validSaveConfig = """
          userAgent = "ua"
          timezone = "Europe/Paris"
          db {
            url = "jdbc:postgresql://localhost:5432/somafm"
            user = "somafm"
            password = "password"
          }
          """;
      SomaFmConfig expectedSomaFmConfig = expectedConfig(
          "ua",
          "Europe/Paris",
          dbConfig("jdbc:postgresql://localhost:5432/somafm", "somafm", "password"),
          null);

      // When
      SomaFmConfig somaFmConfig = ConfigLoader.loadForMode(ConfigFactory.parseString(validSaveConfig), Mode.SAVE);

      // Then
      assertThat(somaFmConfig).usingRecursiveComparison().isEqualTo(expectedSomaFmConfig);
    }

    @Test
    void should_reject_when_db_config_is_missing() {
      // Given
      String saveConfigWithoutDb = """
          userAgent = "ua"
          timezone = "Europe/Paris"
          """;

      // "When"
      ThrowingCallable call = () -> ConfigLoader.loadForMode(ConfigFactory.parseString(saveConfigWithoutDb), Mode.SAVE);

      // Then
      assertThatThrownBy(call)
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("Database config is missing or invalid! Exiting.");
    }

    @Test
    void should_reject_when_db_config_is_incomplete() {
      // Given
      String saveConfigWithoutDbPassword = """
          userAgent = "ua"
          timezone = "Europe/Paris"
          db {
            url = "jdbc:postgresql://localhost:5432/somafm"
            user = "somafm"
          }
          """;

      // "When"
      ThrowingCallable call = () -> ConfigLoader.loadForMode(ConfigFactory.parseString(saveConfigWithoutDbPassword), Mode.SAVE);

      // Then
      assertThatThrownBy(call)
          .isInstanceOf(ConfigException.ValidationFailed.class)
          .hasMessageContaining("password");
    }
  }

  @Nested
  class when_called_in_api_mode {

    @Test
    void should_load_with_db_server_and_scheduler() {
      // Given
      String validApiConfig = """
          userAgent = "ua"
          timezone = "Europe/Paris"
          db {
            url = "jdbc:postgresql://localhost:5432/somafm"
            user = "somafm"
            password = "password"
          }
          api {
            port = 7070
            scheduler {
              enabled = true
              period = "10m"
              channels = ["dronezone"]
            }
          }
          """;
      SomaFmConfig expectedSomaFmConfig = expectedConfig(
          "ua",
          "Europe/Paris",
          dbConfig("jdbc:postgresql://localhost:5432/somafm", "somafm", "password"),
          apiConfig(7070, true, Duration.ofMinutes(10), List.of("dronezone")));

      // When
      SomaFmConfig somaFmConfig = ConfigLoader.loadForMode(ConfigFactory.parseString(validApiConfig), Mode.API);

      // Then
      assertThat(somaFmConfig).usingRecursiveComparison().isEqualTo(expectedSomaFmConfig);
    }

    @Test
    void should_accept_when_scheduler_is_disabled() {
      // Given
      String apiConfigWithoutScheduler = """
          userAgent = "ua"
          timezone = "Europe/Paris"
          db {
            url = "jdbc:postgresql://localhost:5432/somafm"
            user = "somafm"
            password = "password"
          }
          api {
            port = 7070
            scheduler {
              enabled = false
            }
          }
          """;

      // When
      SomaFmConfig somaFmConfig = ConfigLoader.loadForMode(ConfigFactory.parseString(apiConfigWithoutScheduler), Mode.API);

      // Then
      assertThat(somaFmConfig.getApi().isSchedulerEnabled()).isFalse();
    }

    @Test
    void should_reject_when_db_config_is_missing() {
      // Given
      String apiConfigWithoutDb = """
          userAgent = "ua"
          timezone = "Europe/Paris"
          api {
            port = 7070
            scheduler {
              enabled = true
              period = "10m"
              channels = ["dronezone"]
            }
          }
          """;

      // "When"
      ThrowingCallable call = () -> ConfigLoader.loadForMode(ConfigFactory.parseString(apiConfigWithoutDb), Mode.API);

      // Then
      assertThatThrownBy(call)
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("Database config is missing or invalid! Exiting.");
    }

    @Test
    void should_reject_when_db_config_is_incomplete() {
      // Given
      String apiConfigWithoutDbPassword = """
          userAgent = "ua"
          timezone = "Europe/Paris"
          db {
            url = "jdbc:postgresql://localhost:5432/somafm"
            user = "somafm"
          }
          api {
            port = 7070
            scheduler {
              enabled = true
              period = "10m"
              channels = ["dronezone"]
            }
          }
          """;

      // "When"
      ThrowingCallable call = () -> ConfigLoader.loadForMode(ConfigFactory.parseString(apiConfigWithoutDbPassword), Mode.API);

      // Then
      assertThatThrownBy(call)
          .isInstanceOf(ConfigException.ValidationFailed.class)
          .hasMessageContaining("password");
    }

    @Test
    void should_reject_when_server_config_is_missing() {
      // Given
      String apiConfigWithoutServer = """
          userAgent = "ua"
          timezone = "Europe/Paris"
          db {
            url = "jdbc:postgresql://localhost:5432/somafm"
            user = "somafm"
            password = "password"
          }
          """;

      // "When"
      ThrowingCallable call = () -> ConfigLoader.loadForMode(ConfigFactory.parseString(apiConfigWithoutServer), Mode.API);

      // Then
      assertThatThrownBy(call)
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("API config is missing or invalid! Exiting.");
    }

    @Test
    void should_reject_when_server_port_is_invalid() {
      // Given
      String apiConfigWithInvalidServerPort = """
          userAgent = "ua"
          timezone = "Europe/Paris"
          db {
            url = "jdbc:postgresql://localhost:5432/somafm"
            user = "somafm"
            password = "password"
          }
          api {
            port = 0
            scheduler {
              enabled = true
              period = "10m"
              channels = ["dronezone"]
            }
          }
          """;

      // "When"
      ThrowingCallable call = () -> ConfigLoader.loadForMode(ConfigFactory.parseString(apiConfigWithInvalidServerPort), Mode.API);

      // Then
      assertThatThrownBy(call)
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("API config is missing or invalid! Exiting.");
    }

  }

  private static SomaFmConfig expectedConfig(
      String userAgent,
      String timezone,
      DbConfig dbConfig,
      ApiConfig apiConfig
  ) {
    var config = new SomaFmConfig();
    config.setUserAgent(userAgent);
    config.setTimezone(timezone);
    config.setDb(dbConfig);
    config.setApi(apiConfig);
    return config;
  }

  private static DbConfig dbConfig(String url, String user, String password) {
    var dbConfig = new DbConfig();
    dbConfig.setUrl(url);
    dbConfig.setUser(user);
    dbConfig.setPassword(password);
    return dbConfig;
  }

  private static ApiConfig apiConfig(int port, boolean enabled, Duration period, List<String> channels) {
    var apiConfig = new ApiConfig();
    apiConfig.setPort(port);
    var schedulerConfig = new SchedulerConfig();
    schedulerConfig.setEnabled(enabled);
    schedulerConfig.setPeriod(period);
    schedulerConfig.setChannels(channels);
    apiConfig.setScheduler(schedulerConfig);
    return apiConfig;
  }

}
