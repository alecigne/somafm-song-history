package net.lecigne.somafm.adapters.secondary;

import static net.lecigne.somafm.fixtures.TestFixtures.dirkSerriesSongFixture;
import static net.lecigne.somafm.fixtures.TestFixtures.igneousFlameSongFixture;
import static net.lecigne.somafm.recentlib.PredefinedChannel.DRONE_ZONE;
import static org.assertj.core.api.Assertions.assertThat;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import net.lecigne.somafm.fixtures.TestRepository;
import net.lecigne.somafm.recentlib.Broadcast;
import net.lecigne.somafm.recentlib.Song;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DisplayName("The default broadcast repository")
@Tag("integration")
@Testcontainers
class DefaultBroadcastRepositoryIT {

  private static DefaultBroadcastRepository repository;
  private static TestRepository testRepository;

  @Container
  private static final PostgreSQLContainer<?> POSTGRES_CONTAINER = new PostgreSQLContainer<>("postgres:16-alpine");

  @BeforeAll
  static void beforeAll() {
    var hikariConfig = new HikariConfig();
    hikariConfig.setJdbcUrl(POSTGRES_CONTAINER.getJdbcUrl());
    hikariConfig.setUsername(POSTGRES_CONTAINER.getUsername());
    hikariConfig.setPassword(POSTGRES_CONTAINER.getPassword());
    var hikariDataSource = new HikariDataSource(hikariConfig);

    Flyway.configure()
        .dataSource(POSTGRES_CONTAINER.getJdbcUrl(), POSTGRES_CONTAINER.getUsername(), POSTGRES_CONTAINER.getPassword())
        .load()
        .migrate();

    repository = new DefaultBroadcastRepository(null, hikariDataSource);
    testRepository = new TestRepository(hikariDataSource);
  }

  @AfterEach
  void tearDown() throws IOException {
    testRepository.deleteAllData();
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
