package net.lecigne.somafm.history.adapters.out;

import static net.lecigne.somafm.history.fixtures.Fixtures.dirkSerriesSix;
import static net.lecigne.somafm.history.fixtures.Fixtures.igneousFlameIncandescentArc;
import static net.lecigne.somafm.recentlib.PredefinedChannel.DRONE_ZONE;
import static org.assertj.core.api.Assertions.assertThat;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import net.lecigne.somafm.history.domain.model.Broadcast;
import net.lecigne.somafm.history.domain.model.Song;
import net.lecigne.somafm.history.domain.model.SongBroadcast;
import net.lecigne.somafm.history.domain.model.SongDetails;
import net.lecigne.somafm.history.fixtures.Fixtures;
import net.lecigne.somafm.history.fixtures.TestRepository;
import org.assertj.core.api.SoftAssertions;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@DisplayName("The default broadcast repository")
@Tag("integration")
@Testcontainers
class SqlRepositoriesIT {

  private static SqlBroadcastRepository broadcastRepo;
  private static SqlSongRepository songRepo;
  private static TestRepository testRepo;

  @Container
  private static final PostgreSQLContainer POSTGRES_CONTAINER = new PostgreSQLContainer("postgres:16-alpine");

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
    broadcastRepo = new SqlBroadcastRepository(hikariDataSource);
    songRepo = new SqlSongRepository(hikariDataSource);
    testRepo = new TestRepository(hikariDataSource);
  }

  @AfterEach
  void tearDown() throws IOException {
    testRepo.deleteAllData();
  }

  @Test
  void should_persist_broadcasts() throws IOException {
    // Given
    Broadcast broadcast1 = Broadcast.builder()
        .time(Instant.parse("2021-01-01T11:36:43.123Z"))
        .channel(DRONE_ZONE)
        .song(dirkSerriesSix())
        .build();
    Broadcast broadcast2 = Broadcast.builder()
        .time(Instant.parse("2021-01-01T11:45:37.967Z"))
        .channel(DRONE_ZONE)
        .song(igneousFlameIncandescentArc())
        .build();

    // When
    broadcastRepo.updateBroadcasts(List.of(broadcast1, broadcast2));

    // Then
    List<Broadcast> broadcasts = testRepo.readAllBroadcasts();
    assertThat(broadcasts)
        .hasSize(2)
        .usingRecursiveFieldByFieldElementComparatorIgnoringFields("song.id")
        .containsExactlyInAnyOrder(broadcast1, broadcast2);
  }

  @Test
  void should_not_insert_same_broadcast_twice_and_ignore_failure() throws IOException {
    // Given
    Broadcast broadcast1 = Broadcast.builder()
        .time(Instant.parse("2021-01-01T11:36:43.123Z"))
        .channel(DRONE_ZONE)
        .song(dirkSerriesSix())
        .build();
    Broadcast broadcast2 = Broadcast.builder()
        .time(Instant.parse("2021-01-01T11:36:43.123Z"))
        .channel(DRONE_ZONE)
        .song(dirkSerriesSix())
        .build();

    // When
    broadcastRepo.updateBroadcasts(List.of(broadcast1, broadcast2));

    // Then
    List<Broadcast> broadcasts = testRepo.readAllBroadcasts();
    assertThat(broadcasts)
        .hasSize(1)
        .usingRecursiveFieldByFieldElementComparatorIgnoringFields("song.id")
        .containsExactlyInAnyOrder(broadcast1);
  }

  @Test
  void should_not_insert_same_song_twice() throws IOException {
    // Given - 2 broadcasts of the same song
    Broadcast broadcast1 = Broadcast.builder()
        .time(Instant.parse("2021-01-01T11:36:43.123Z"))
        .channel(DRONE_ZONE)
        .song(dirkSerriesSix())
        .build();
    Broadcast broadcast2 = Broadcast.builder()
        .time(Instant.parse("2021-01-02T14:50:50.420Z"))
        .channel(DRONE_ZONE)
        .song(dirkSerriesSix())
        .build();

    // When
    broadcastRepo.updateBroadcasts(List.of(broadcast1, broadcast2));

    // Then
    List<Broadcast> broadcasts = testRepo.readAllBroadcasts();
    assertThat(broadcasts)
        .hasSize(2)
        .usingRecursiveFieldByFieldElementComparatorIgnoringFields("song.id")
        .containsExactlyInAnyOrder(broadcast1, broadcast2);

    List<Song> songs = testRepo.readAllSongs();
    assertThat(songs)
        .hasSize(1)
        .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
        .containsExactlyInAnyOrder(dirkSerriesSix());
  }

  @Test
  void should_get_broadcasts_with_pagination() {
    // Given
    var oldest = Broadcast.builder()
        .time(Instant.parse("2021-01-01T11:36:43.123Z"))
        .channel(DRONE_ZONE)
        .song(dirkSerriesSix())
        .build();
    var middle = Broadcast.builder()
        .time(Instant.parse("2021-01-01T11:45:37.967Z"))
        .channel(DRONE_ZONE)
        .song(igneousFlameIncandescentArc())
        .build();
    var newest = Broadcast.builder()
        .time(Instant.parse("2021-01-01T11:50:00.000Z"))
        .channel(DRONE_ZONE)
        .song(igneousFlameIncandescentArc())
        .build();
    broadcastRepo.updateBroadcasts(List.of(oldest, middle, newest));

    // When
    List<Broadcast> firstPage = broadcastRepo.getBroadcasts(1, 2);
    List<Broadcast> secondPage = broadcastRepo.getBroadcasts(2, 2);
    long total = broadcastRepo.countBroadcasts();

    // Then
    assertThat(firstPage).usingRecursiveFieldByFieldElementComparatorIgnoringFields("song.id").containsExactly(newest, middle);
    assertThat(secondPage).usingRecursiveFieldByFieldElementComparatorIgnoringFields("song.id").containsExactly(oldest);
    assertThat(total).isEqualTo(3);
  }

  @Test
  void should_get_ordered_songs_with_pagination() {
    // Given
    var dirkSerriesSong = Broadcast.builder()
        .time(Instant.parse("2021-01-01T11:36:43.123Z"))
        .channel(DRONE_ZONE)
        .song(Fixtures.dirkSerriesSix())
        .build();
    var igneousFlameRegenerativeShifts = Broadcast.builder()
        .time(Instant.parse("2021-01-01T11:45:37.967Z"))
        .channel(DRONE_ZONE)
        .song(Fixtures.igneousFlameRegenerativeShifts())
        .build();
    var igneousFlameIncandescentArc = Broadcast.builder()
        .time(Instant.parse("2021-01-01T11:50:12.198Z"))
        .channel(DRONE_ZONE)
        .song(Fixtures.igneousFlameIncandescentArc())
        .build();
    broadcastRepo.updateBroadcasts(List.of(dirkSerriesSong, igneousFlameIncandescentArc, igneousFlameRegenerativeShifts));

    // When
    List<Song> firstPage = songRepo.getSongs(1, 2);
    List<Song> secondPage = songRepo.getSongs(2, 2);

    // Then
    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(firstPage)
          .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
          .containsExactly(dirkSerriesSong.song(), igneousFlameIncandescentArc.song());
      softly.assertThat(secondPage)
          .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
          .containsExactly(igneousFlameRegenerativeShifts.song());
    });
  }

  @Test
  void should_get_song_with_broadcasts_ordered_by_time_desc() {
    // Given
    var oldest = Broadcast.builder()
        .time(Instant.parse("2021-01-01T11:36:43.123Z"))
        .channel(DRONE_ZONE)
        .song(dirkSerriesSix())
        .build();
    var newest = Broadcast.builder()
        .time(Instant.parse("2021-01-02T14:50:50.420Z"))
        .channel(DRONE_ZONE)
        .song(dirkSerriesSix())
        .build();
    var otherSong = Broadcast.builder()
        .time(Instant.parse("2021-01-03T10:00:00.000Z"))
        .channel(DRONE_ZONE)
        .song(igneousFlameIncandescentArc())
        .build();
    broadcastRepo.updateBroadcasts(List.of(oldest, newest, otherSong));
    Long songId = songRepo.getSongs(1, 10).stream()
        .filter(song -> song.title().equals(dirkSerriesSix().title()))
        .findFirst()
        .orElseThrow()
        .id();

    // When
    Optional<SongDetails> details = songRepo.getSong(songId);

    // Then
    assertThat(details).isPresent();
    assertThat(details.orElseThrow().song())
        .usingRecursiveComparison()
        .ignoringFields("id")
        .isEqualTo(dirkSerriesSix());
    assertThat(details.orElseThrow().broadcasts()).containsExactly(
        new SongBroadcast(newest.time(), DRONE_ZONE),
        new SongBroadcast(oldest.time(), DRONE_ZONE));
  }

  @Test
  void should_return_empty_when_song_does_not_exist() {
    assertThat(songRepo.getSong(404L)).isEmpty();
  }

}
