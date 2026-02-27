package net.lecigne.somafm.history.bootstrap.config;

import static net.lecigne.somafm.history.bootstrap.config.SomaFmConfig.ROOT_CONFIG;

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
    SomaFmConfig somaFmConfig = fromTypeSafeConfig(ConfigFactory.load());
    validateDbConfig(mode, somaFmConfig);
    validateServerConfig(mode, somaFmConfig);
    return somaFmConfig;
  }

  private static void validateDbConfig(Mode mode, SomaFmConfig somaFmConfig) {
    if (mode.needsDatabase() && !somaFmConfig.isDbConfigured()) {
      throw new IllegalStateException("DB-backed modes require db config! Exiting.");
    }
  }

  private static void validateServerConfig(Mode mode, SomaFmConfig somaFmConfig) {
    if (Mode.API.equals(mode) && !somaFmConfig.isServerConfigured()) {
      throw new IllegalStateException("API mode requires server config! Exiting.");
    }
  }

  public static SomaFmConfig loadForMode(String resourceBasename) {
    return fromTypeSafeConfig(ConfigFactory.load(resourceBasename));
  }

  static SomaFmConfig fromTypeSafeConfig(Config typeSafeConfig) {
    Config normalized = typeSafeConfig.getConfig(ROOT_CONFIG);
    return ConfigBeanFactory.create(normalized, SomaFmConfig.class);
  }

}
