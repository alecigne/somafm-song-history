package net.lecigne.somafm.history.bootstrap.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigBeanFactory;
import com.typesafe.config.ConfigFactory;
import lombok.extern.slf4j.Slf4j;
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
    validateModeConfig(mode, somaFmConfig);
    return somaFmConfig;
  }

  private static void validateModeConfig(Mode mode, SomaFmConfig somaFmConfig) {
    switch (mode) {
      case DISPLAY -> {
        // No validation necessary in DISPLAY mode
      }
      case SAVE -> validateDbConfig(somaFmConfig);
      case API -> {
        validateDbConfig(somaFmConfig);
        validateServerConfig(somaFmConfig);
        validateSchedulerConfig(somaFmConfig);
      }
    }
  }

  private static void validateDbConfig(SomaFmConfig somaFmConfig) {
    if (!somaFmConfig.isDbConfigured()) {
      throw new IllegalStateException("DB-backed modes require db config! Exiting.");
    }
  }

  private static void validateServerConfig(SomaFmConfig somaFmConfig) {
    if (!somaFmConfig.isServerConfigured()) {
      throw new IllegalStateException("API mode requires server config! Exiting.");
    }
  }

  private static void validateSchedulerConfig(SomaFmConfig somaFmConfig) {
    if (!somaFmConfig.isSchedulerConfigured()) {
      throw new IllegalStateException("API mode requires scheduler config! Exiting.");
    }
  }

}
