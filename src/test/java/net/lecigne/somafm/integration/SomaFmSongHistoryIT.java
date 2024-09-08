package net.lecigne.somafm.integration;

import static net.lecigne.somafm.SomaFmSongHistory.BROADCAST_LOCATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

import com.google.common.io.Resources;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import net.lecigne.somafm.business.RecentBroadcastBusiness;
import net.lecigne.somafm.cli.CLI;
import net.lecigne.somafm.fixtures.TestFixtures;
import net.lecigne.somafm.fixtures.TestRepository;
import net.lecigne.somafm.mappers.DisplayedBroadcastMapper;
import net.lecigne.somafm.recentlib.Broadcast;
import net.lecigne.somafm.recentlib.Channel;
import net.lecigne.somafm.recentlib.SomaFm;
import net.lecigne.somafm.recentlib.Song;
import net.lecigne.somafm.repository.BroadcastRepository;
import net.lecigne.somafm.repository.DefaultBroadcastRepository;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

// TODO [persist-recent-broadcasts] Mutualize resources between this test and DefaultBroadcastRepositoryIT
@DisplayName("The application")
@Testcontainers
class SomaFmSongHistoryIT {

  private static SomaFm mockSomaFmClient = Mockito.mock(SomaFm.class);
  private static CLI cli;
  private static TestRepository testRepository;

  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
      "postgres:16-alpine"
  );

  @BeforeAll
  static void beforeAll() {
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
    BroadcastRepository repository = new DefaultBroadcastRepository(mockSomaFmClient, hikariDataSource);
    var business = new RecentBroadcastBusiness(repository, new DisplayedBroadcastMapper(BROADCAST_LOCATION));
    testRepository = new TestRepository(hikariDataSource);
    cli = new CLI(business);
  }

  @AfterEach
  void tearDown() throws IOException {
    testRepository.deleteAllData();
  }

  @Test
  void should_get_recent_broadcasts_and_persist_them_with_no_song_duplicates() throws IOException {
    // Given
    String[] args = {"save", "Drone Zone"};
    BDDMockito
        .given(mockSomaFmClient.fetchRecent(any()))
        .willReturn(List.of(
            Broadcast.builder()
                .channel(Channel.DRONE_ZONE)
                .time(LocalDateTime.parse("2018-04-29T12:36:43").atZone(ZoneId.of("Europe/Paris")).toInstant())
                .song(Song.builder().artist("Dirk Serries' Microphonics").title("VI").album("microphonics VI - XX").build())
                .build(),
            Broadcast.builder()
                .channel(Channel.DRONE_ZONE)
                .time(LocalDateTime.parse("2018-04-29T12:26:56").atZone(ZoneId.of("Europe/Paris")).toInstant())
                .song(Song.builder().artist("Igneous Flame").title("Incandescent Arc").album("Lapiz").build())
                .build(),
            Broadcast.builder()
                .channel(Channel.DRONE_ZONE)
                .time(LocalDateTime.parse("2018-04-29T12:10:30").atZone(ZoneId.of("Europe/Paris")).toInstant())
                .song(Song.builder().artist("Dirk Serries' Microphonics").title("VI").album("microphonics VI - XX").build())
                .build()
        ));

    // When
    cli.run(args);

    // Then
    List<Broadcast> broadcasts = testRepository.readAllBroadcasts();
    List<Song> songs = testRepository.readAllSongs();
    assertThat(broadcasts).hasSize(3);
    assertThat(songs).hasSize(2);
  }

}
