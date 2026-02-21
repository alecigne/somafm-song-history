package net.lecigne.somafm.history.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.google.common.io.Resources;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import net.lecigne.somafm.history.adapters.in.cli.CLI;
import net.lecigne.somafm.history.adapters.out.HtmlSomaFmRepository;
import net.lecigne.somafm.history.adapters.out.SqlBroadcastRepository;
import net.lecigne.somafm.history.application.ports.in.SomaFmSongHistory;
import net.lecigne.somafm.history.application.ports.out.BroadcastRepository;
import net.lecigne.somafm.history.application.ports.out.SomaFmRepository;
import net.lecigne.somafm.history.application.services.DefaultSomaFmSongHistory;
import net.lecigne.somafm.history.bootstrap.config.SomaFmConfig;
import net.lecigne.somafm.history.fixtures.TestRepository;
import net.lecigne.somafm.recentlib.Broadcast;
import net.lecigne.somafm.recentlib.SomaFm;
import net.lecigne.somafm.recentlib.Song;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

// TODO Mutualize resources between this test and DefaultBroadcastRepositoryIT
// TODO Mock the SomaFM client, it is the lib's responsability to run its own integration tests
@DisplayName("The application")
@Testcontainers
class SomaFmSongHistoryIT {

  static WireMockServer wireMockServer;
  private static CLI cli;
  private static TestRepository testRepo;

  @Container
  private static final PostgreSQLContainer POSTGRES_CONTAINER = new PostgreSQLContainer("postgres:16-alpine");

  @BeforeAll
  static void beforeAll() throws IOException {
    // WireMock
    wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
    wireMockServer.start();
    configureStubs();

    // Persistence
    var hikariConfig = new HikariConfig();
    hikariConfig.setJdbcUrl(POSTGRES_CONTAINER.getJdbcUrl());
    hikariConfig.setUsername(POSTGRES_CONTAINER.getUsername());
    hikariConfig.setPassword(POSTGRES_CONTAINER.getPassword());
    var hikariDataSource = new HikariDataSource(hikariConfig);

    Flyway.configure()
          .dataSource(POSTGRES_CONTAINER.getJdbcUrl(), POSTGRES_CONTAINER.getUsername(), POSTGRES_CONTAINER.getPassword())
          .load()
          .migrate();

    // Application
    var somaFmConfig = new SomaFmConfig();
    somaFmConfig.setTimezone("Europe/Paris");
    var somaFm = SomaFm.of(wireMockServer.baseUrl(), "UA");
    SomaFmRepository somaFmRepository = HtmlSomaFmRepository.init(somaFm);
    BroadcastRepository repository = SqlBroadcastRepository.init(hikariDataSource);
    SomaFmSongHistory init = DefaultSomaFmSongHistory.init(repository, somaFmRepository);
    testRepo = new TestRepository(hikariDataSource);
    cli = CLI.init(init, somaFmConfig);
  }

  @AfterEach
  void tearDown() throws IOException {
    testRepo.deleteAllData();
  }

  @AfterAll
  static void afterAll() {
    wireMockServer.stop();
  }

  @Test
  void should_get_recent_broadcasts_and_persist_them_with_no_song_duplicates() throws IOException {
    // Given
    String[] args = {"save", "Drone Zone"};

    // When
    cli.run(args);

    // Then
    List<Broadcast> broadcasts = testRepo.readAllBroadcasts();
    List<Song> songs = testRepo.readAllSongs();
    assertThat(broadcasts).hasSize(20);
    assertThat(songs).hasSize(19);
  }

  private static void configureStubs() throws IOException {
    URL url = Resources.getResource("data/dronezone_with_one_duplicate.html");
    String html = Resources.toString(url, StandardCharsets.UTF_8);
    wireMockServer.stubFor(get(urlMatching(".*"))
        .willReturn(aResponse().withStatus(200).withBody(html)));
  }

}
