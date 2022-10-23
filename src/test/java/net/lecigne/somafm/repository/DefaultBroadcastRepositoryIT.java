package net.lecigne.somafm.repository;

import static net.lecigne.somafm.fixtures.TestFixtures.dirkSerriesSongFixture;
import static net.lecigne.somafm.model.Channel.DRONE_ZONE;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Set;
import net.lecigne.somafm.client.HtmlBroadcastsClient;
import net.lecigne.somafm.client.HtmlBroadcastsParser;
import net.lecigne.somafm.client.RecentBroadcastsClient;
import net.lecigne.somafm.config.Configuration;
import net.lecigne.somafm.mappers.BroadcastMapper;
import net.lecigne.somafm.model.Broadcast;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The default broadcast repository")
class DefaultBroadcastRepositoryIT {

  static MockWebServer mockWebServer;
  private static BroadcastRepository repository;

  @BeforeAll
  static void beforeAll() throws IOException {
    mockWebServer = new MockWebServer();
    mockWebServer.start();
    mockWebServer.setDispatcher(getDispatcher());
    Configuration configuration = new Configuration();
    configuration.setSomaFmBaseUrl(mockWebServer.url("/").toString());
    configuration.setUserAgent("UA");
    HtmlBroadcastsClient htmlClient = HtmlBroadcastsClient.create(configuration);
    var recentBroadcastsClient = new RecentBroadcastsClient(htmlClient, new HtmlBroadcastsParser());
    Clock clock = Clock.fixed(Instant.parse("2021-01-01T13:00:00.00Z"), ZoneId.of("Europe/Paris"));
    repository = new DefaultBroadcastRepository(recentBroadcastsClient, new BroadcastMapper(clock));
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
