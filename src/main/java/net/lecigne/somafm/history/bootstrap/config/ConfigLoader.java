package net.lecigne.somafm.history.bootstrap.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigBeanFactory;
import com.typesafe.config.ConfigFactory;
import lombok.extern.slf4j.Slf4j;
import net.lecigne.somafm.history.bootstrap.config.SomaFmConfig.ApiConfig;
import net.lecigne.somafm.history.bootstrap.config.SomaFmConfig.DbConfig;
import net.lecigne.somafm.history.domain.model.Mode;

@Slf4j
public final class ConfigLoader {

  private ConfigLoader() {
  }

  public static SomaFmConfig loadForMode(Mode mode) {
    return loadForMode(ConfigFactory.load(), mode);
  }

  static SomaFmConfig loadForMode(Config config, Mode mode) {
    SomaFmConfig somaFmConfig = ConfigBeanFactory.create(config, SomaFmConfig.class);
    validateForMode(somaFmConfig, mode);
    return somaFmConfig;
  }

  private static void validateForMode(SomaFmConfig somaFmConfig, Mode mode) {
    switch (mode) {
      case DISPLAY -> {
        // No validation necessary in DISPLAY mode
      }
      case SAVE -> validateDbConfig(somaFmConfig.getDb());
      case API -> {
        validateDbConfig(somaFmConfig.getDb());
        valideApiConfig(somaFmConfig.getApi());
      }
    }
  }

  private static void validateDbConfig(DbConfig dbConfig) {
    if (dbConfig == null || !dbConfig.isOk()) {
      throw new IllegalStateException("Database config is missing or invalid! Exiting.");
    }
  }

  private static void valideApiConfig(ApiConfig apiConfig) {
    if (apiConfig == null || !apiConfig.isOk()) {
      throw new IllegalStateException("API config is missing or invalid! Exiting.");
    }
  }

}
