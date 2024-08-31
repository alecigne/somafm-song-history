package net.lecigne.somafm;

import static net.lecigne.somafm.config.SomaFmConfig.ROOT_CONFIG;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigBeanFactory;
import com.typesafe.config.ConfigFactory;
import java.time.ZoneId;
import lombok.extern.slf4j.Slf4j;
import net.lecigne.somafm.business.BusinessAction;
import net.lecigne.somafm.business.RecentBroadcastBusiness;
import net.lecigne.somafm.cli.CLI;
import net.lecigne.somafm.config.SomaFmConfig;
import net.lecigne.somafm.config.SomaFmConfig.DbConfig;
import org.flywaydb.core.Flyway;

@Slf4j
public class SomaFmSongHistory {

  /**
   * SomaFM's broadcast location.
   */
  public static final ZoneId BROADCAST_LOCATION = ZoneId.of("America/Los_Angeles");

  /**
   * The title given to breaks by SomaFM.
   */
  public static final String BREAK_STATION_ID = "Break / Station ID";

  public static void main(String[] args) {
    Config config = ConfigFactory.load();
    SomaFmConfig somaFmConfig = ConfigBeanFactory.create(config.getConfig(ROOT_CONFIG), SomaFmConfig.class);
    BusinessAction action = BusinessAction.getValue(args[0]);
    somaFmConfig.setAction(action);
    if (BusinessAction.SAVE.equals(action)) {
      prepareDatabase(somaFmConfig);
    }
    RecentBroadcastBusiness business = RecentBroadcastBusiness.init(somaFmConfig);
    new CLI(business).run(args);
  }

  private static void prepareDatabase(SomaFmConfig somaFmConfig) {
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

}
