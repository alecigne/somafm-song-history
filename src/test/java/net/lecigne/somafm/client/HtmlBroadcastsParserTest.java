package net.lecigne.somafm.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.List;
import net.lecigne.somafm.client.dto.BroadcastDto;
import net.lecigne.somafm.exception.SomaFmHtmlParsingException;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The SomaFM HTML broadcast parser")
class HtmlBroadcastsParserTest {

  private static final HtmlBroadcastsParser PARSER = new HtmlBroadcastsParser();

  @Test
  void should_parse_correctly() throws IOException {
    // Given
    URL url = Resources.getResource("data/dronezone.html");
    String text = Resources.toString(url, StandardCharsets.UTF_8);
    BroadcastDto expected = BroadcastDto.builder()
        .time(LocalTime.of(3, 36, 43))
        .artist("Dirk Serries' Microphonics")
        .title("VI")
        .album("microphonics VI - XX")
        .build();

    // When
    List<BroadcastDto> broadcasts = PARSER.parse(text);

    // Then
    assertThat(broadcasts).hasSize(20);
    BroadcastDto broadcastDto = broadcasts.get(0);
    assertThat(broadcastDto).isEqualTo(expected);
  }

  @Test
  void should_throw_when_no_table() throws IOException {
    // Given
    URL url = Resources.getResource("data/dronezone_no_table.html");
    String text = Resources.toString(url, StandardCharsets.UTF_8);

    // When
    ThrowingCallable throwingCallable = () -> PARSER.parse(text);

    // Then
    assertThatThrownBy(throwingCallable)
        .isInstanceOf(SomaFmHtmlParsingException.class)
        .hasMessage("Table element was not found in HTML source!");
  }

}
