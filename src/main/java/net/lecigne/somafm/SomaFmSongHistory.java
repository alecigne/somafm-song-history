package net.lecigne.somafm;

import static net.lecigne.somafm.config.SomaFmConfig.ROOT_CONFIG;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigBeanFactory;
import com.typesafe.config.ConfigFactory;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import net.lecigne.somafm.adapters.primary.CLI;
import net.lecigne.somafm.adapters.secondary.DefaultBroadcastRepository;
import net.lecigne.somafm.application.api.SomaFmSongHistoryApi;
import net.lecigne.somafm.application.logic.RecentBroadcastBusiness;
import net.lecigne.somafm.application.spi.SomaFmSongHistorySpi;
import net.lecigne.somafm.config.SomaFmConfig;
import net.lecigne.somafm.config.SomaFmConfig.DbConfig;
import net.lecigne.somafm.domain.Action;
import net.lecigne.somafm.recentlib.SomaFm;
import org.flywaydb.core.Flyway;

@Slf4j
public class SomaFmSongHistory {

  public static void main(String[] args) {
    Config config = ConfigFactory.load();
    SomaFmConfig somaFmConfig = ConfigBeanFactory.create(config.getConfig(ROOT_CONFIG), SomaFmConfig.class);
    Action action = Action.getValue(args[0]);
    somaFmConfig.setAction(action);
    if (Action.SAVE.equals(action)) {
      initDb(somaFmConfig);
    }
    SomaFmSongHistorySpi spi = initSpi(somaFmConfig);
    SomaFmSongHistoryApi api = initBusiness(spi);
    CLI cli = CLI.initCli(api, somaFmConfig);
    cli.run(args);
  }

  private static void initDb(SomaFmConfig somaFmConfig) {
    if (!somaFmConfig.isDbActivated()) {
      log.error("SAVE mode requires db config! Exiting.");
      System.exit(1);
    } else {
      DbConfig db = somaFmConfig.getDb();
      Flyway.configure()
          .dataSource(db.getUrl(), db.getUser(), db.getPassword())
          .load()
          .migrate();
    }
  }

  private static SomaFmSongHistorySpi initSpi(SomaFmConfig config) {
    HikariDataSource hikariDataSource;
    if (Action.SAVE.equals(config.getAction())) {
      var hikariConfig = new HikariConfig();
      DbConfig db = config.getDb();
      hikariConfig.setJdbcUrl(db.getUrl());
      hikariConfig.setUsername(db.getUser());
      hikariConfig.setPassword(db.getPassword());
      hikariDataSource = new HikariDataSource(hikariConfig);
    } else {
      hikariDataSource = null;
    }
    SomaFm somaFm = SomaFm.of(config.getUserAgent());
    return new DefaultBroadcastRepository(somaFm, hikariDataSource);
  }

  private static RecentBroadcastBusiness initBusiness(SomaFmSongHistorySpi spi) {
    return new RecentBroadcastBusiness(spi);
  }

}
