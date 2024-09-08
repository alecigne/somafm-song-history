package net.lecigne.somafm.repository;

import static net.lecigne.somafm.fixtures.TestFixtures.dirkSerriesSongFixture;
import static net.lecigne.somafm.fixtures.TestFixtures.igneousFlameSongFixture;
import static net.lecigne.somafm.recentlib.Channel.DRONE_ZONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import net.lecigne.somafm.fixtures.TestRepository;
import net.lecigne.somafm.recentlib.Broadcast;
import net.lecigne.somafm.recentlib.Channel;
import net.lecigne.somafm.recentlib.SomaFm;
import net.lecigne.somafm.recentlib.Song;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@DisplayName("The default broadcast repository")
@Testcontainers
class DefaultBroadcastRepositoryIT {

  private static SomaFm mockSomaFmClient = Mockito.mock(SomaFm.class);
  private static BroadcastRepository repository;
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
    repository = new DefaultBroadcastRepository(mockSomaFmClient, hikariDataSource);
    testRepository = new TestRepository(hikariDataSource);
  }

  @AfterEach
  void tearDown() throws IOException {
    testRepository.deleteAllData();
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
    BDDMockito
        .given(mockSomaFmClient.fetchRecent(any()))
        .willReturn(List.of(
            expectedMostRecentBroadcast,
            Broadcast.builder()
                .channel(Channel.DRONE_ZONE)
                .time(LocalDateTime.parse("2018-04-29T12:26:56").atZone(ZoneId.of("Europe/Paris")).toInstant())
                .song(Song.builder().artist("Igneous Flame").title("Incandescent Arc").album("Lapiz").build())
                .build(),
            Broadcast.builder()
                .channel(Channel.DRONE_ZONE)
                .time(LocalDateTime.parse("2018-04-29T12:20:05").atZone(ZoneId.of("Europe/Paris")).toInstant())
                .song(Song.builder().artist("Snufmumriko").title("Further Afield").album("This Tide Will Bring You Home").build())
                .build()
        ));

    // When
    List<Broadcast> recentBroadcasts = repository.getRecentBroadcasts(channel);

    // Then
    assertThat(recentBroadcasts)
        .isNotEmpty()
        .hasSize(3);
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
    repository.updateBroadcasts(List.of(broadcast1, broadcast2));

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
    repository.updateBroadcasts(List.of(broadcast1, broadcast2));

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
    repository.updateBroadcasts(List.of(broadcast1, broadcast2));

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

}
