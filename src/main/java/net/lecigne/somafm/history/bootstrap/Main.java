package net.lecigne.somafm.history.bootstrap;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import lombok.extern.slf4j.Slf4j;
import net.lecigne.somafm.history.adapters.in.cli.CLI;
import net.lecigne.somafm.history.adapters.in.rest.JavalinRestController;
import net.lecigne.somafm.history.adapters.in.scheduler.SaveScheduler;
import net.lecigne.somafm.history.adapters.out.HtmlSomaFmRepository;
import net.lecigne.somafm.history.adapters.out.SqlBroadcastRepository;
import net.lecigne.somafm.history.application.ports.out.BroadcastRepository;
import net.lecigne.somafm.history.application.ports.out.SomaFmRepository;
import net.lecigne.somafm.history.application.services.SomaFmSongHistoryService;
import net.lecigne.somafm.history.bootstrap.config.ConfigLoader;
import net.lecigne.somafm.history.bootstrap.config.SomaFmConfig;
import net.lecigne.somafm.history.bootstrap.config.SomaFmConfig.ApiConfig;
import net.lecigne.somafm.history.bootstrap.config.SomaFmConfig.DbConfig;
import net.lecigne.somafm.history.domain.model.Mode;
import net.lecigne.somafm.recentlib.SomaFm;
import org.flywaydb.core.Flyway;

@Slf4j
public class Main {

  public static void main(String[] args) {
    if (args.length == 0) {
      log.error("You must enter at least 1 argument: mode.");
      return;
    }
    Mode mode = Mode.getValue(args[0]);
    SomaFmConfig somaFmConfig = ConfigLoader.loadForMode(mode);
    SomaFm somaFmClient = SomaFm.of(somaFmConfig.getUserAgent());
    SomaFmRepository somaFmRepo = HtmlSomaFmRepository.init(somaFmClient);
    BroadcastRepository broadcastRepo = null;
    if (mode.needsDatabase()) {
      initDb(somaFmConfig);
      broadcastRepo = SqlBroadcastRepository.init(somaFmConfig);
    }
    SomaFmSongHistoryService service = SomaFmSongHistoryService.init(broadcastRepo, somaFmRepo);
    switch (mode) {
      case SAVE, DISPLAY -> CLI.init(service, somaFmConfig).run(args);
      case API -> initApiMode(service, somaFmConfig.getApi());
    }
  }

  private static void initApiMode(SomaFmSongHistoryService service, ApiConfig apiConfig) {
    if (apiConfig.isSchedulerEnabled()) {
      SaveScheduler.init(service, apiConfig.getScheduler());
    }
    JavalinRestController controller = JavalinRestController.init(service, service);
    Javalin
        .create(config -> {
          config.jsonMapper(new JavalinJackson().updateMapper(mapper -> {
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
          }));
          config.routes.apiBuilder(controller.routes());
        })
        .start(apiConfig.getPort());
    log.info("API server started on port {}", apiConfig.getPort());
  }

  private static void initDb(SomaFmConfig somaFmConfig) {
    DbConfig dbConfig = somaFmConfig.getDb();
    Flyway.configure()
        .dataSource(dbConfig.getUrl(), dbConfig.getUser(), dbConfig.getPassword())
        .load()
        .migrate();
  }

}
