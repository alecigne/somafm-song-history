package net.lecigne.somafm.history.bootstrap;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.json.JavalinJackson;
import lombok.extern.slf4j.Slf4j;
import net.lecigne.somafm.history.adapters.in.cli.CLI;
import net.lecigne.somafm.history.adapters.in.rest.JavalinRestController;
import net.lecigne.somafm.history.adapters.in.scheduler.SaveScheduler;
import net.lecigne.somafm.history.adapters.out.HtmlSomaFmRepository;
import net.lecigne.somafm.history.adapters.out.SqlBroadcastRepository;
import net.lecigne.somafm.history.adapters.out.SqlSongRepository;
import net.lecigne.somafm.history.application.ports.out.BroadcastRepository;
import net.lecigne.somafm.history.application.ports.out.SomaFmRepository;
import net.lecigne.somafm.history.application.ports.out.SongRepository;
import net.lecigne.somafm.history.application.services.SomaFmCommandDispatcher;
import net.lecigne.somafm.history.application.services.SomaFmHistoryService;
import net.lecigne.somafm.history.application.services.SomaFmRecentService;
import net.lecigne.somafm.history.bootstrap.config.ConfigLoader;
import net.lecigne.somafm.history.bootstrap.config.SomaFmConfig;
import net.lecigne.somafm.history.bootstrap.config.SomaFmConfig.ApiConfig;
import net.lecigne.somafm.history.bootstrap.config.SomaFmConfig.DbConfig;
import net.lecigne.somafm.history.application.model.Mode;
import net.lecigne.somafm.recentlib.SomaFm;
import org.flywaydb.core.Flyway;

@Slf4j
public class Main {

  public static void main(String[] args) {
    if (args.length == 0) {
      log.atError()
          .addKeyValue("operation", "app.start")
          .log("You must enter at least 1 argument: mode.");
      return;
    }
    Mode mode = Mode.getValue(args[0]);
    log.atInfo()
        .addKeyValue("operation", "app.start")
        .addKeyValue("mode", mode.name())
        .log("Starting application");
    SomaFmConfig somaFmConfig = ConfigLoader.loadForMode(mode);
    SomaFm somaFmClient = SomaFm.of(somaFmConfig.getUserAgent());
    SomaFmRepository somaFmRepo = HtmlSomaFmRepository.init(somaFmClient);
    switch (mode) {
      case DISPLAY -> initCliMode(somaFmRepo, null, somaFmConfig).run(args);
      case SAVE -> {
        initDb(somaFmConfig);
        BroadcastRepository broadcastRepo = SqlBroadcastRepository.init(somaFmConfig);
        initCliMode(somaFmRepo, broadcastRepo, somaFmConfig).run(args);
      }
      case API -> {
        initDb(somaFmConfig);
        BroadcastRepository broadcastRepo = SqlBroadcastRepository.init(somaFmConfig);
        SongRepository songRepo = SqlSongRepository.init(somaFmConfig);
        SomaFmRecentService recentService = SomaFmRecentService.init(somaFmRepo, broadcastRepo);
        SomaFmHistoryService historyService = SomaFmHistoryService.init(broadcastRepo, songRepo);
        initApiMode(recentService, historyService, somaFmConfig.getApi());
      }
    }
  }

  private static CLI initCliMode(
      SomaFmRepository somaFmRepo,
      BroadcastRepository broadcastRepo,
      SomaFmConfig somaFmConfig) {
    SomaFmRecentService recentService = SomaFmRecentService.init(somaFmRepo, broadcastRepo);
    SomaFmCommandDispatcher commandDispatcher = SomaFmCommandDispatcher.init(recentService, recentService);
    return CLI.init(commandDispatcher, somaFmConfig);
  }

  private static void initApiMode(
      SomaFmRecentService recentService,
      SomaFmHistoryService historyService,
      ApiConfig apiConfig) {
    if (apiConfig.isSchedulerEnabled()) {
      SaveScheduler.init(recentService, apiConfig.getScheduler());
    }
    JavalinRestController controller = JavalinRestController.init(historyService, historyService, historyService, recentService);
    Javalin
        .create(config -> {
          config.jsonMapper(new JavalinJackson().updateMapper(mapper -> {
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
          }));
          config.staticFiles.add("/public", Location.CLASSPATH);
          config.routes.apiBuilder(controller.routes());
        })
        .start(apiConfig.getPort());
    log.atInfo()
        .addKeyValue("operation", "api.start")
        .addKeyValue("port", apiConfig.getPort())
        .log("API server started");
  }

  private static void initDb(SomaFmConfig somaFmConfig) {
    DbConfig dbConfig = somaFmConfig.getDb();
    Flyway.configure()
        .dataSource(dbConfig.getUrl(), dbConfig.getUser(), dbConfig.getPassword())
        .load()
        .migrate();
  }

}
