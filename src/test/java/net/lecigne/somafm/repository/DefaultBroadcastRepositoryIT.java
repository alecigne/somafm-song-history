package net.lecigne.somafm.repository;

import static net.lecigne.somafm.fixtures.TestFixtures.dirkSerriesSongFixture;
import static net.lecigne.somafm.fixtures.TestFixtures.igneousFlameSongFixture;
import static net.lecigne.somafm.model.Channel.DRONE_ZONE;
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
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import net.lecigne.somafm.client.HtmlBroadcastsClient;
import net.lecigne.somafm.client.HtmlBroadcastsParser;
import net.lecigne.somafm.client.RecentBroadcastsClient;
import net.lecigne.somafm.config.SomaFmConfig;
import net.lecigne.somafm.fixtures.TestRepository;
import net.lecigne.somafm.mappers.BroadcastMapper;
import net.lecigne.somafm.model.Broadcast;
import net.lecigne.somafm.model.Song;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@DisplayName("The default broadcast repository")
@Testcontainers
class DefaultBroadcastRepositoryIT {

  static MockWebServer mockWebServer;
  private static BroadcastRepository repository;
  private static TestRepository testRepository;

  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
      "postgres:16-alpine"
  );

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
    var configuration = new SomaFmConfig();
    configuration.setSomaFmBaseUrl(mockWebServer.url("/").toString());
    configuration.setUserAgent("UA");
    configuration.setTimezone("Europe/Paris");
    HtmlBroadcastsClient htmlClient = HtmlBroadcastsClient.create(configuration);
    var recentBroadcastsClient = new RecentBroadcastsClient(htmlClient, new HtmlBroadcastsParser());
    Clock clock = Clock.fixed(Instant.parse("2021-01-01T13:00:00.00Z"), ZoneId.of("Europe/Paris"));
    repository = new DefaultBroadcastRepository(recentBroadcastsClient, new BroadcastMapper(clock), hikariDataSource);
    testRepository = new TestRepository(hikariDataSource);
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
  void should_get_most_recent_broadcasts() throws IOException {
    // Given
    var channel = DRONE_ZONE;
    var expectedMostRecentBroadcast = Broadcast.builder()
        .time(Instant.parse("2021-01-01T11:36:43.00Z"))
        .channel(channel)
        .song(dirkSerriesSongFixture())
        .build();

    // When
    Set<Broadcast> recentBroadcasts = repository.getRecentBroadcasts(channel);

    // Then
    assertThat(recentBroadcasts)
        .isNotEmpty()
        .hasSize(20);
    Broadcast mostRecentBroadcast = recentBroadcasts.stream().max(Comparator.comparing(Broadcast::getTime)).get();
    assertThat(mostRecentBroadcast).usingRecursiveComparison().isEqualTo(expectedMostRecentBroadcast);
  }

  @Test
  void should_persist_broadcasts() throws IOException {
    // Given
    Broadcast broadcast1 = Broadcast.builder()
        .time(Instant.parse("2021-01-01T11:36:43.123Z"))
        .channel(DRONE_ZONE)
        .song(dirkSerriesSongFixture())
        .build();
    Broadcast broadcast2 = Broadcast.builder()
        .time(Instant.parse("2021-01-01T11:45:37.967Z"))
        .channel(DRONE_ZONE)
        .song(igneousFlameSongFixture())
        .build();

    // When
    repository.updateBroadcasts(Set.of(broadcast1, broadcast2));

    // Then
    List<Broadcast> broadcasts = testRepository.readAllBroadcasts();
    assertThat(broadcasts)
        .hasSize(2)
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactlyInAnyOrder(broadcast1, broadcast2);
  }

  @Test
  void should_not_insert_same_broadcast_twice_and_ignore_failure() throws IOException {
    // Given
    Broadcast broadcast1 = Broadcast.builder()
        .time(Instant.parse("2021-01-01T11:36:43.123Z"))
        .channel(DRONE_ZONE)
        .song(dirkSerriesSongFixture())
        .build();
    Broadcast broadcast2 = Broadcast.builder()
        .time(Instant.parse("2021-01-01T11:36:43.123Z"))
        .channel(DRONE_ZONE)
        .song(dirkSerriesSongFixture())
        .build();

    // When
    repository.updateBroadcasts(Set.of(broadcast1, broadcast2));

    // Then
    List<Broadcast> broadcasts = testRepository.readAllBroadcasts();
    assertThat(broadcasts)
        .hasSize(1)
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactlyInAnyOrder(broadcast1);
  }

  @Test
  void should_not_insert_same_song_twice() throws IOException {
    // Given - 2 broadcasts of the same song
    Broadcast broadcast1 = Broadcast.builder()
        .time(Instant.parse("2021-01-01T11:36:43.123Z"))
        .channel(DRONE_ZONE)
        .song(dirkSerriesSongFixture())
        .build();
    Broadcast broadcast2 = Broadcast.builder()
        .time(Instant.parse("2021-01-02T14:50:50.420Z"))
        .channel(DRONE_ZONE)
        .song(dirkSerriesSongFixture())
        .build();

    // When
    repository.updateBroadcasts(Set.of(broadcast1, broadcast2));

    // Then
    List<Broadcast> broadcasts = testRepository.readAllBroadcasts();
    assertThat(broadcasts)
        .hasSize(2)
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactlyInAnyOrder(broadcast1, broadcast2);

    List<Song> songs = testRepository.readAllSongs();
    assertThat(songs)
        .hasSize(1)
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactlyInAnyOrder(dirkSerriesSongFixture());
  }

  private static Dispatcher getDispatcher() throws IOException {
    URL url = Resources.getResource("data/dronezone.html");
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
