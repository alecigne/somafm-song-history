package net.lecigne.somafm.history.bootstrap;

import static net.lecigne.somafm.history.bootstrap.config.SomaFmConfig.ROOT_CONFIG;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigBeanFactory;
import com.typesafe.config.ConfigFactory;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.lecigne.somafm.history.adapters.in.cli.CLI;
import net.lecigne.somafm.history.adapters.in.rest.JavalinRestController;
import net.lecigne.somafm.history.adapters.in.scheduler.SaveScheduler;
import net.lecigne.somafm.history.adapters.out.HtmlSomaFmRepository;
import net.lecigne.somafm.history.adapters.out.SqlBroadcastRepository;
import net.lecigne.somafm.history.application.ports.in.RunCommandUseCase;
import net.lecigne.somafm.history.application.ports.out.BroadcastRepository;
import net.lecigne.somafm.history.application.ports.out.SomaFmRepository;
import net.lecigne.somafm.history.application.services.SomaFmSongHistoryService;
import net.lecigne.somafm.history.bootstrap.config.SomaFmConfig;
import net.lecigne.somafm.history.bootstrap.config.SomaFmConfig.DbConfig;
import net.lecigne.somafm.history.domain.model.Mode;
import net.lecigne.somafm.recentlib.Channel;
import net.lecigne.somafm.recentlib.PredefinedChannel;
import net.lecigne.somafm.recentlib.SomaFm;
import org.flywaydb.core.Flyway;

@Slf4j
public class Main {

  public static void main(String[] args) {
    if (args.length == 0) {
      log.error("You must enter at least 1 argument: mode.");
      return;
    }
    Config config = ConfigFactory.load();
    SomaFmConfig somaFmConfig = ConfigBeanFactory.create(config.getConfig(ROOT_CONFIG), SomaFmConfig.class);
    if ("api".equalsIgnoreCase(args[0])) {
      runApiServer(somaFmConfig);
      return;
    }
    Mode mode = Mode.getValue(args[0]);
    BroadcastRepository broadcastRepository = null;
    if (Mode.SAVE.equals(mode)) {
      initDb(somaFmConfig);
      broadcastRepository = SqlBroadcastRepository.init(somaFmConfig);
    }
    SomaFm somaFmClient = SomaFm.of(somaFmConfig.getUserAgent());
    SomaFmRepository somaFmRepo = HtmlSomaFmRepository.init(somaFmClient);
    RunCommandUseCase runCommandUseCase = SomaFmSongHistoryService.init(broadcastRepository, somaFmRepo);
    CLI cli = CLI.init(runCommandUseCase, somaFmConfig);
    cli.run(args);
  }

  private static void initDb(SomaFmConfig somaFmConfig) {
    if (!somaFmConfig.isDbActivated()) {
      log.error("DB-backed modes require db config! Exiting.");
      System.exit(1);
    } else {
      DbConfig db = somaFmConfig.getDb();
      Flyway.configure()
          .dataSource(db.getUrl(), db.getUser(), db.getPassword())
          .load()
          .migrate();
    }
  }

  private static void runApiServer(SomaFmConfig somaFmConfig) {
    initDb(somaFmConfig);
    BroadcastRepository broadcastRepository = SqlBroadcastRepository.init(somaFmConfig);
    SomaFm somaFmClient = SomaFm.of(somaFmConfig.getUserAgent());
    SomaFmRepository somaFmRepo = HtmlSomaFmRepository.init(somaFmClient);
    SomaFmSongHistoryService service = SomaFmSongHistoryService.init(broadcastRepository, somaFmRepo);
    Javalin apiServer = Javalin.create(config -> config.jsonMapper(
        new JavalinJackson().updateMapper(mapper -> {
          mapper.registerModule(new JavaTimeModule());
          mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        })));
    JavalinRestController javalinRestController = JavalinRestController.init(service, service);
    javalinRestController.registerRoutes(apiServer);
    SaveScheduler saveScheduler = startSaveScheduler(somaFmConfig, service);
    apiServer.start(7070);
    log.info("API server started on port 7070");
    if (saveScheduler != null) {
      Runtime.getRuntime().addShutdownHook(new Thread(saveScheduler::shutdown, "save-scheduler-shutdown"));
    }
  }

  private static SaveScheduler startSaveScheduler(SomaFmConfig somaFmConfig, SomaFmSongHistoryService service) {
    if (!somaFmConfig.isSchedulerActivated()) {
      log.info("Save scheduler disabled.");
      return null;
    }
    var schedulerConfig = somaFmConfig.getScheduler();
    List<Channel> channels = schedulerConfig
        .getChannels()
        .stream()
        .map(Main::mapToChannel)
        .toList();
    SaveScheduler saveScheduler = SaveScheduler.init(service, schedulerConfig.getPeriod());
    saveScheduler.schedule(channels);
    return saveScheduler;
  }

  private static Channel mapToChannel(String configuredName) {
    return PredefinedChannel
        .getByInternalName(configuredName)
        .orElseThrow(() -> new IllegalArgumentException("Unknown scheduler channel: " + configuredName));
  }

}
