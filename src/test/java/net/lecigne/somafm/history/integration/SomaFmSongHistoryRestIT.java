package net.lecigne.somafm.history.integration;

import static io.restassured.RestAssured.given;
import static net.lecigne.somafm.history.fixtures.Fixtures.breakSongFixture;
import static net.lecigne.somafm.history.fixtures.Fixtures.dirkSerriesSix;
import static net.lecigne.somafm.history.fixtures.Fixtures.igneousFlameIncandescentArc;
import static net.lecigne.somafm.recentlib.PredefinedChannel.DRONE_ZONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.json.JavalinJackson;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import net.lecigne.somafm.history.adapters.in.rest.HttpRequestLogging;
import net.lecigne.somafm.history.adapters.in.rest.JavalinRestController;
import net.lecigne.somafm.history.application.model.Page;
import net.lecigne.somafm.history.application.ports.in.FetchRecentBroadcastsUseCase;
import net.lecigne.somafm.history.application.ports.in.GetBroadcastsUseCase;
import net.lecigne.somafm.history.application.ports.in.GetSongDetailsUseCase;
import net.lecigne.somafm.history.application.ports.in.GetSongsUseCase;
import net.lecigne.somafm.history.domain.model.Broadcast;
import net.lecigne.somafm.history.domain.model.Song;
import net.lecigne.somafm.history.domain.model.SongBroadcast;
import net.lecigne.somafm.history.domain.model.SongDetails;
import net.lecigne.somafm.recentlib.Channel;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The SomaFM Song History REST API")
class SomaFmSongHistoryRestIT {

  private final FakeGetBroadcastsUseCase getBroadcastsUseCase = new FakeGetBroadcastsUseCase();
  private final FakeGetSongsUseCase getSongsUseCase = new FakeGetSongsUseCase();
  private final FakeGetSongDetailsUseCase getSongDetailsUseCase = new FakeGetSongDetailsUseCase();
  private final FakeFetchRecentBroadcastsUseCase fetchRecentBroadcastsUseCase = new FakeFetchRecentBroadcastsUseCase();
  private Javalin app;
  private int port;

  @BeforeEach
  void setUp() {
    JavalinRestController controller = JavalinRestController.init(
        getBroadcastsUseCase,
        getSongsUseCase,
        getSongDetailsUseCase,
        fetchRecentBroadcastsUseCase);
    app = Javalin.create(config -> {
      config.jsonMapper(new JavalinJackson().updateMapper(mapper -> {
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
      }));
      config.staticFiles.add("/public", Location.CLASSPATH);
      config.routes.apiBuilder(controller.routes());
    });
    app.start(0);
    port = app.port();
  }

  @AfterEach
  void tearDown() {
    app.stop();
    getBroadcastsUseCase.reset();
    getSongsUseCase.reset();
    getSongDetailsUseCase.reset();
    fetchRecentBroadcastsUseCase.reset();
  }

  @Test
  void should_serve_web_ui() {
    // Given
    RequestSpecification req = given().port(port);

    // When
    Response response = req.get("/");

    // Then
    String body = response.then().statusCode(200).extract().asString();
    assertThat(body)
        .contains("<title>SomaFM Song History</title>")
        .contains("app.js");
  }

  @Test
  void should_get_broadcasts() {
    // Given
    Broadcast first = broadcastAt("2021-01-01T11:10:00Z", dirkSerriesSix());
    Broadcast second = broadcastAt("2021-01-01T11:00:00Z", breakSongFixture());
    getBroadcastsUseCase.page = new Page<>(2, 2, 5, 3, List.of(first, second));
    RequestSpecification req = given().port(port);

    // When
    Response response = req.get("/broadcasts");

    // Then
    response.then()
        .statusCode(200)
        .body("page", equalTo(2))
        .body("size", equalTo(2))
        .body("totalElements", equalTo(5))
        .body("totalPages", equalTo(3))
        .body("items", hasSize(2))
        .body("items[0].time", equalTo("2021-01-01T11:10:00Z"))
        .body("items[0].channel", equalTo("Drone Zone"))
        .body("items[0].song.id", equalTo(1))
        .body("items[0].song.artist", equalTo(dirkSerriesSix().artist()))
        .body("items[0].song.title", equalTo(dirkSerriesSix().title()))
        .body("items[1].song.title", equalTo(breakSongFixture().title()));
  }

  @Test
  void should_use_default_pagination_parameters() {
    // Given
    RequestSpecification req = given().port(port);

    // When
    Response response = req.get("/broadcasts");

    // Then
    response.then().statusCode(200);
    assertThat(getBroadcastsUseCase.receivedPage).isEqualTo(1);
    assertThat(getBroadcastsUseCase.receivedSize).isEqualTo(50);
  }

  @Test
  void should_propagate_request_id_and_log_request_completion() {
    // Given
    String requestId = "test-request-id";
    RequestSpecification req = given()
        .port(port)
        .header("X-Request-Id", requestId);

    try (LogCaptor logCaptor = LogCaptor.forClass(HttpRequestLogging.class)) {
      // When
      Response response = req.get("/broadcasts");

      // Then
      response.then()
          .statusCode(200)
          .header("X-Request-Id", requestId);
      assertThat(logCaptor.getInfoLogs()).contains("HTTP request completed");
    }
  }

  @Test
  void should_generate_request_id_when_header_is_missing() {
    // Given
    RequestSpecification req = given().port(port);

    // When
    Response response = req.get("/broadcasts");

    // Then
    String requestId = response.then()
        .statusCode(200)
        .extract()
        .header("X-Request-Id");
    assertThat(requestId).isNotBlank();
  }

  @Test
  void should_return_bad_request_when_page_query_param_is_not_an_integer() {
    // Given
    RequestSpecification req = given().port(port);

    // When
    Response response = req.queryParam("page", "abc").get("/broadcasts");

    // Then
    String body = response.then().statusCode(400).extract().asString();
    assertThat(body).contains("Query parameter 'page' must be an integer");
  }

  @Test
  void should_return_bad_request_when_get_broadcasts_use_case_rejects_request() {
    // Given
    var err = "size must be <= 50";
    getBroadcastsUseCase.error = new IllegalArgumentException(err);
    RequestSpecification req = given().port(port);

    // When
    Response response = req.get("/broadcasts");

    // Then
    String body = response.then().statusCode(400).extract().asString();
    assertThat(body).contains(err);
  }

  @Test
  void should_get_songs() {
    // Given
    getSongsUseCase.page = new Page<>(2, 2, 5, 3, List.of(
        songWithId(dirkSerriesSix(), 1L),
        songWithId(igneousFlameIncandescentArc(), 2L)));
    RequestSpecification req = given().port(port);

    // When
    Response response = req.get("/songs");

    // Then
    response.then()
        .statusCode(200)
        .body("page", equalTo(2))
        .body("size", equalTo(2))
        .body("totalElements", equalTo(5))
        .body("totalPages", equalTo(3))
        .body("items", hasSize(2))
        .body("items[0].id", equalTo(1))
        .body("items[0].artist", equalTo(dirkSerriesSix().artist()))
        .body("items[0].title", equalTo(dirkSerriesSix().title()))
        .body("items[0].album", equalTo(dirkSerriesSix().album()))
        .body("items[1].artist", equalTo(igneousFlameIncandescentArc().artist()))
        .body("items[1].title", equalTo(igneousFlameIncandescentArc().title()));
  }

  @Test
  void should_get_song_details() {
    // Given
    var song = Song.builder()
        .id(42L)
        .artist("Dirk Serries' Microphonics")
        .title("VI")
        .album("microphonics VI - XX")
        .build();

    // Details to return upon calling the fake service
    getSongDetailsUseCase.songDetails = new SongDetails(
        song,
        List.of(
            new SongBroadcast(Instant.parse("2021-01-02T10:00:00Z"), DRONE_ZONE),
            new SongBroadcast(Instant.parse("2021-01-01T11:00:00Z"), DRONE_ZONE)));
    RequestSpecification req = given().port(port);

    // When
    Response response = req.get("/songs/42");

    // Then
    response.then()
        .statusCode(200)
        .body("id", equalTo(42))
        .body("artist", equalTo(song.artist()))
        .body("title", equalTo(song.title()))
        .body("album", equalTo(song.album()))
        .body("broadcasts", hasSize(2))
        .body("broadcasts[0].time", equalTo("2021-01-02T10:00:00Z"))
        .body("broadcasts[0].channel", equalTo("Drone Zone"))
        .body("broadcasts[1].time", equalTo("2021-01-01T11:00:00Z"));
    assertThat(getSongDetailsUseCase.receivedId).isEqualTo(42L);
  }

  @Test
  void should_return_not_found_when_song_does_not_exist() {
    // Given
    RequestSpecification req = given().port(port);

    // When
    Response response = req.get("/songs/42");

    // Then
    String body = response.then().statusCode(404).extract().asString();
    assertThat(body).contains("Song with ID #42 not found");
  }

  @Test
  void should_return_bad_request_when_song_id_is_not_an_integer() {
    // Given
    RequestSpecification req = given().port(port);

    // When
    Response response = req.get("/songs/abc");

    // Then
    String body = response.then().statusCode(400).extract().asString();
    assertThat(body).contains("Path parameter 'id' must be an integer");
  }

  @Test
  void should_use_default_pagination_parameters_for_songs() {
    // Given
    RequestSpecification req = given().port(port);

    // When
    Response response = req.get("/songs");

    // Then
    response.then().statusCode(200);
    assertThat(getSongsUseCase.receivedPage).isEqualTo(1);
    assertThat(getSongsUseCase.receivedSize).isEqualTo(50);
  }

  @Test
  void should_return_bad_request_when_get_songs_use_case_rejects_request() {
    // Given
    var err = "size must be <= 50";
    getSongsUseCase.error = new IllegalArgumentException(err);
    RequestSpecification req = given().port(port);

    // When
    Response response = req.get("/songs");

    // Then
    String body = response.then().statusCode(400).extract().asString();
    assertThat(body).contains(err);
  }

  @Test
  void should_get_recent_broadcasts_for_channel() {
    // Given
    Broadcast broadcast = broadcastAt("2021-01-01T11:10:00Z", dirkSerriesSix());
    fetchRecentBroadcastsUseCase.broadcasts = List.of(broadcast);
    RequestSpecification req = given().port(port);

    // When
    Response response = req.queryParam("channel", "dronezone").get("/broadcasts/recent");

    // Then
    response.then()
        .statusCode(200)
        .body("", hasSize(1))
        .body("[0].time", equalTo("2021-01-01T11:10:00Z"))
        .body("[0].channel", equalTo("Drone Zone"))
        .body("[0].song.artist", equalTo(dirkSerriesSix().artist()))
        .body("[0].song.title", equalTo(dirkSerriesSix().title()));
    assertThat(fetchRecentBroadcastsUseCase.receivedChannel).isEqualTo(DRONE_ZONE);
  }

  @Test
  void should_return_bad_request_when_fetch_recent_use_case_rejects_request() {
    // Given
    fetchRecentBroadcastsUseCase.error = new IllegalArgumentException("Unknown scheduler channel: dronezone");
    RequestSpecification req = given().port(port);

    // When
    Response response = req.queryParam("channel", "dronezone").get("/broadcasts/recent");

    // Then
    String body = response.then()
        .statusCode(400)
        .extract()
        .asString();
    assertThat(body).contains("Unknown scheduler channel: dronezone");
  }

  private static Broadcast broadcastAt(String time, Song song) {
    return Broadcast.builder()
        .time(Instant.parse(time))
        .channel(DRONE_ZONE)
        .song(song.id() == null ? songWithId(song, 1L) : song)
        .build();
  }

  private static Song songWithId(Song song, long id) {
    return new Song(id, song.artist(), song.title(), song.album());
  }

  private static final class FakeGetBroadcastsUseCase implements GetBroadcastsUseCase {

    private int receivedPage;
    private int receivedSize;
    private Page<Broadcast> page = new Page<>(1, 50, 0, 0, List.of());
    private IllegalArgumentException error;

    @Override
    public Page<Broadcast> getBroadcasts(int page, int size) {
      receivedPage = page;
      receivedSize = size;
      if (error != null) throw error;
      return this.page;
    }

    private void reset() {
      receivedPage = 0;
      receivedSize = 0;
      page = new Page<>(1, 50, 0, 0, List.of());
      error = null;
    }

  }

  private static final class FakeFetchRecentBroadcastsUseCase implements FetchRecentBroadcastsUseCase {

    private Channel receivedChannel;
    private List<Broadcast> broadcasts = List.of();
    private IllegalArgumentException error;

    @Override
    public List<Broadcast> fetchRecent(Channel channel) {
      receivedChannel = channel;
      if (error != null) throw error;
      return broadcasts;
    }

    private void reset() {
      receivedChannel = null;
      broadcasts = List.of();
      error = null;
    }

  }

  private static final class FakeGetSongDetailsUseCase implements GetSongDetailsUseCase {

    private Long receivedId;
    private SongDetails songDetails = null;

    @Override
    public Optional<SongDetails> getSongDetails(long id) {
      receivedId = id;
      return Optional.ofNullable(songDetails);
    }

    private void reset() {
      receivedId = null;
      songDetails = null;
    }

  }

  private static final class FakeGetSongsUseCase implements GetSongsUseCase {

    private int receivedPage;
    private int receivedSize;
    private Page<Song> page = new Page<>(1, 50, 0, 0, List.of());
    private IllegalArgumentException error;

    @Override
    public Page<Song> getSongs(int page, int size) {
      receivedPage = page;
      receivedSize = size;
      if (error != null) throw error;
      return this.page;
    }

    private void reset() {
      receivedPage = 0;
      receivedSize = 0;
      page = new Page<>(1, 50, 0, 0, List.of());
      error = null;
    }

  }

}
