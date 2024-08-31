package net.lecigne.somafm.config;

import com.typesafe.config.Optional;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.lecigne.somafm.business.BusinessAction;

@NoArgsConstructor
@Getter
@Setter
public class SomaFmConfig {

  public static final String ROOT_CONFIG = "config";

  private String somaFmBaseUrl;
  private String userAgent;
  private String timezone;
  @Optional
  private DbConfig db;
  @Optional
  private BusinessAction action; // set internally

  @NoArgsConstructor
  @Getter
  @Setter
  public static class DbConfig {
    private String url;
    private String user;
    private String password;
  }

  public boolean isDbActivated() {
    return Objects.nonNull(db) &&
           Stream.of(db.getUrl(), db.getUser(), db.getPassword()).allMatch(Objects::nonNull);
  }

}
