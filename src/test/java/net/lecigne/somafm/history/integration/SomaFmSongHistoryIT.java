package net.lecigne.somafm.history.integration;

import static net.lecigne.somafm.history.fixtures.TestFixtures.breakSongFixture;
import static net.lecigne.somafm.history.fixtures.TestFixtures.dirkSerriesSongFixture;
import static net.lecigne.somafm.history.fixtures.TestFixtures.igneousFlameSongFixture;
import static net.lecigne.somafm.recentlib.PredefinedChannel.DRONE_ZONE;
import static org.assertj.core.api.Assertions.assertThat;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.time.Instant;
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
import net.lecigne.somafm.recentlib.PredefinedChannel;
import net.lecigne.somafm.recentlib.SomaFm;
import net.lecigne.somafm.recentlib.Song;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

// TODO Mutualize resources between this test and DefaultBroadcastRepositoryIT
@DisplayName("The application")
@Testcontainers
@TestInstance(Lifecycle.PER_CLASS)
class SomaFmSongHistoryIT {

  private CLI cli;
  private TestRepository testRepo;
  private final SomaFm somaFm = Mockito.mock(SomaFm.class);

  @Container
  private static final PostgreSQLContainer POSTGRES_CONTAINER = new PostgreSQLContainer("postgres:16-alpine");

  @BeforeAll
  void beforeAll() {
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

  @Test
  void should_get_recent_broadcasts_and_persist_them_with_no_song_duplicates() throws IOException {
    // Given
    String[] args = {"save", "Drone Zone"};
    BDDMockito
        .given(somaFm.fetchRecent(PredefinedChannel.DRONE_ZONE))
        .willReturn(List.of(
            Broadcast.builder()
                     .time(Instant.parse("2021-01-01T13:00:00.00Z"))
                     .channel(DRONE_ZONE)
                     .song(dirkSerriesSongFixture())
                     .build(),
            // Same song played twice
            Broadcast.builder()
                     .time(Instant.parse("2021-01-01T13:02:00.00Z"))
                     .channel(DRONE_ZONE)
                     .song(dirkSerriesSongFixture())
                     .build(),
            Broadcast.builder()
                     .time(Instant.parse("2021-01-01T13:15:00.00Z"))
                     .channel(DRONE_ZONE)
                     .song(igneousFlameSongFixture())
                     .build(),
            Broadcast.builder()
                     .time(Instant.parse("2021-01-01T13:20:00.00Z"))
                     .channel(DRONE_ZONE)
                     .song(breakSongFixture())
                     .build()));

    // When
    cli.run(args);

    // Then
    List<Broadcast> broadcasts = testRepo.readAllBroadcasts();
    List<Song> songs = testRepo.readAllSongs();
    assertThat(broadcasts).hasSize(4);
    assertThat(songs).hasSize(3);
  }

}
