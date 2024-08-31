package net.lecigne.somafm.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;
import net.lecigne.somafm.config.SomaFmConfig.DbConfig;
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
    boolean isDbActivated = config.isDbActivated();

    // Then
    assertThat(isDbActivated).isEqualTo(expected);
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

}
