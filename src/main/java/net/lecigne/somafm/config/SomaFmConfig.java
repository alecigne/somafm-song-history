package net.lecigne.somafm.config;

import java.time.Duration;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class SomaFmConfig {
  public static final String ROOT_CONFIG = "config";
  private String somaFmBaseUrl;
  private String userAgent;
  private Duration interval;
  private String dbUrl;
  private String dbUser;
  private String dbPassword;
}
