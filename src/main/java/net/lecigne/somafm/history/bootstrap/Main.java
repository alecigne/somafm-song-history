package net.lecigne.somafm.history.bootstrap;

import static net.lecigne.somafm.history.bootstrap.config.SomaFmConfig.ROOT_CONFIG;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigBeanFactory;
import com.typesafe.config.ConfigFactory;
import lombok.extern.slf4j.Slf4j;
import net.lecigne.somafm.history.adapters.in.cli.CLI;
import net.lecigne.somafm.history.adapters.out.HtmlSomaFmRepository;
import net.lecigne.somafm.history.adapters.out.SqlBroadcastRepository;
import net.lecigne.somafm.history.application.ports.in.SomaFmSongHistory;
import net.lecigne.somafm.history.application.ports.out.BroadcastRepository;
import net.lecigne.somafm.history.application.ports.out.SomaFmRepository;
import net.lecigne.somafm.history.application.services.DefaultSomaFmSongHistory;
import net.lecigne.somafm.history.bootstrap.config.SomaFmConfig;
import net.lecigne.somafm.history.bootstrap.config.SomaFmConfig.DbConfig;
import net.lecigne.somafm.history.domain.Action;
import net.lecigne.somafm.recentlib.SomaFm;
import org.flywaydb.core.Flyway;

@Slf4j
public class Main {

  public static void main(String[] args) {
    Config config = ConfigFactory.load();
    SomaFmConfig somaFmConfig = ConfigBeanFactory.create(config.getConfig(ROOT_CONFIG), SomaFmConfig.class);
    Action action = Action.getValue(args[0]);
    BroadcastRepository broadcastRepository = null;
    if (Action.SAVE.equals(action)) {
      initDb(somaFmConfig);
      broadcastRepository = SqlBroadcastRepository.init(somaFmConfig);
    }
    SomaFm somaFmClient = SomaFm.of(somaFmConfig.getUserAgent());
    SomaFmRepository somaFmRepo = HtmlSomaFmRepository.init(somaFmClient);
    SomaFmSongHistory api = DefaultSomaFmSongHistory.init(broadcastRepository, somaFmRepo);
    CLI cli = CLI.init(api, somaFmConfig);
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

}
