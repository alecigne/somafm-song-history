package net.lecigne.somafm.config;

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
  private String dbUrl;
  private String dbUser;
  private String dbPassword;
}
