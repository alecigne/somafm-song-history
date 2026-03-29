package net.lecigne.somafm.history.integration;

import static io.restassured.RestAssured.given;
import static net.lecigne.somafm.history.fixtures.TestFixtures.breakSongFixture;
import static net.lecigne.somafm.history.fixtures.TestFixtures.dirkSerriesSongFixture;
import static net.lecigne.somafm.recentlib.PredefinedChannel.DRONE_ZONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.time.Instant;
import java.util.List;
import net.lecigne.somafm.history.adapters.in.rest.JavalinRestController;
import net.lecigne.somafm.history.application.ports.in.FetchRecentBroadcastsUseCase;
import net.lecigne.somafm.history.application.ports.in.GetBroadcastsUseCase;
import net.lecigne.somafm.history.domain.model.Page;
import net.lecigne.somafm.recentlib.Broadcast;
import net.lecigne.somafm.recentlib.Channel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The SomaFM Song History REST API")
class SomaFmSongHistoryRestIT {

  private final FakeGetBroadcastsUseCase getBroadcastsUseCase = new FakeGetBroadcastsUseCase();
  private final FakeFetchRecentBroadcastsUseCase fetchRecentBroadcastsUseCase = new FakeFetchRecentBroadcastsUseCase();
  private Javalin app;
  private int port;

  @BeforeEach
  void setUp() {
    app = Javalin.create(config -> config.jsonMapper(
        new JavalinJackson().updateMapper(mapper -> {
          mapper.registerModule(new JavaTimeModule());
          mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        })));
    JavalinRestController controller = JavalinRestController.init(getBroadcastsUseCase, fetchRecentBroadcastsUseCase);
    controller.registerRoutes(app);
    app.start(0);
    port = app.port();
  }

  @AfterEach
  void tearDown() {
    app.stop();
    getBroadcastsUseCase.reset();
    fetchRecentBroadcastsUseCase.reset();
  }

  @Test
  void should_get_broadcasts() {
    // Given
    Broadcast first = broadcastAt("2021-01-01T11:10:00Z", dirkSerriesSongFixture());
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
        .body("items[0].song.artist", equalTo(dirkSerriesSongFixture().artist().name()))
        .body("items[0].song.title", equalTo(dirkSerriesSongFixture().title()))
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
  void should_get_recent_broadcasts_for_channel() {
    // Given
    Broadcast broadcast = broadcastAt("2021-01-01T11:10:00Z", dirkSerriesSongFixture());
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
        .body("[0].song.artist", equalTo(dirkSerriesSongFixture().artist().name()))
        .body("[0].song.title", equalTo(dirkSerriesSongFixture().title()));
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

  private static Broadcast broadcastAt(String time, net.lecigne.somafm.recentlib.Song song) {
    return Broadcast.builder()
        .time(Instant.parse(time))
        .channel(DRONE_ZONE)
        .song(song)
        .build();
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

}
