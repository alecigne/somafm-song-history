package net.lecigne.somafm.integration;

import static net.lecigne.somafm.SomaFmSongHistory.BROADCAST_LOCATION;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.Resources;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import net.lecigne.somafm.business.RecentBroadcastBusiness;
import net.lecigne.somafm.cli.CLI;
import net.lecigne.somafm.client.HtmlBroadcastsClient;
import net.lecigne.somafm.client.HtmlBroadcastsParser;
import net.lecigne.somafm.client.RecentBroadcastsClient;
import net.lecigne.somafm.config.SomaFmConfig;
import net.lecigne.somafm.fixtures.TestRepository;
import net.lecigne.somafm.mappers.BroadcastMapper;
import net.lecigne.somafm.mappers.DisplayedBroadcastMapper;
import net.lecigne.somafm.model.Broadcast;
import net.lecigne.somafm.model.Song;
import net.lecigne.somafm.repository.BroadcastRepository;
import net.lecigne.somafm.repository.DefaultBroadcastRepository;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.flywaydb.core.Flyway;
import org.junit.Rule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

// TODO [persist-recent-broadcasts] Mutualize resources between this test and DefaultBroadcastRepositoryIT
@DisplayName("The application")
public class SomaFmSongHistoryIT {

  static MockWebServer mockWebServer;
  private static CLI CLI;
  private static TestRepository testRepository;

  @Rule
  public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres");

  @BeforeAll
  static void beforeAll() throws IOException {
    // MockWebServer
    mockWebServer = new MockWebServer();
    mockWebServer.start();
    mockWebServer.setDispatcher(getDispatcher());

    // Persistence
    postgres.start();
    var hikariConfig = new HikariConfig();
    hikariConfig.setJdbcUrl(postgres.getJdbcUrl());
    hikariConfig.setUsername(postgres.getUsername());
    hikariConfig.setPassword(postgres.getPassword());
    var hikariDataSource = new HikariDataSource(hikariConfig);

    Flyway.configure()
        .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
        .load()
        .migrate();

    // Application
    SomaFmConfig configuration = new SomaFmConfig();
    configuration.setSomaFmBaseUrl(mockWebServer.url("/").toString());
    configuration.setUserAgent("UA");
    HtmlBroadcastsClient htmlClient = HtmlBroadcastsClient.create(configuration);
    var recentBroadcastsClient = new RecentBroadcastsClient(htmlClient, new HtmlBroadcastsParser());
    Clock clock = Clock.fixed(Instant.parse("2021-01-01T13:00:00.00Z"), ZoneId.of("Europe/Paris"));
    BroadcastRepository repository = new DefaultBroadcastRepository(recentBroadcastsClient, new BroadcastMapper(clock),
        hikariDataSource);
    var business = new RecentBroadcastBusiness(repository, new DisplayedBroadcastMapper(BROADCAST_LOCATION));
    testRepository = new TestRepository(hikariDataSource);
    CLI = new CLI(business);
  }

  @AfterEach
  void tearDown() throws IOException {
    testRepository.deleteAllData();
  }

  @AfterAll
  static void afterAll() throws IOException {
    mockWebServer.shutdown();
  }

  @Test
  void should_get_recent_broadcasts_and_persist_them_with_no_song_duplicates() throws IOException {
    // Given
    String[] args = {"save", "Drone Zone"};

    // When
    CLI.run(args);

    // Then
    List<Broadcast> broadcasts = testRepository.readAllBroadcasts();
    List<Song> songs = testRepository.readAllSongs();
    assertThat(broadcasts).hasSize(20);
    assertThat(songs).hasSize(19);
  }

  private static Dispatcher getDispatcher() throws IOException {
    URL url = Resources.getResource("data/dronezone_with_one_duplicate.html");
    String html = Resources.toString(url, StandardCharsets.UTF_8);
    return new Dispatcher() {
      @Override
      public MockResponse dispatch(RecordedRequest recordedRequest) {
        return new MockResponse()
            .setResponseCode(200)
            .setBody(html);
      }
    };
  }

}
