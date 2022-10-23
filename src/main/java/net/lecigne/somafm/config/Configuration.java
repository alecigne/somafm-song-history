package net.lecigne.somafm.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class Configuration {
  public static final String ROOT_CONFIG = "config";
  private String somaFmBaseUrl;
  private String userAgent;
}
