package net.lecigne.somafm.client;

import static net.lecigne.somafm.SomaFmSongHistory.BREAK_STATION_ID;

import java.time.LocalTime;
import java.util.List;
import net.lecigne.somafm.client.dto.BroadcastDto;
import net.lecigne.somafm.exception.SomaFmHtmlParsingException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Parse a list of recent broadcasts from HTML to a collection of DTOs.
 */
public class HtmlBroadcastsParser {

  List<BroadcastDto> parse(String htmlBroadcasts) {
    Document doc = Jsoup.parse(htmlBroadcasts);
    Element table = doc.select("table").first();
    if (table == null) {
      throw new SomaFmHtmlParsingException("Table element was not found in HTML source!");
    }
    Elements elements = table.select("tr");
    return elements
        .subList(2, elements.size() - 1)
        .stream()
        .map(this::parseRow)
        .toList();
  }

  private BroadcastDto parseRow(Element row) {
    Elements columns = row.select("td");
    var localTime = LocalTime.parse(columns.get(0).text().substring(0, 8)); // Just keep HH:MM:SS
    var secondField = columns.get(1).text(); // Second field can either be the artist or an announcement
    if (BREAK_STATION_ID.equals(secondField)) {
      return BroadcastDto.builder()
          .time(localTime)
          .artist("n/a")
          .title(secondField)
          .album("n/a")
          .build();
    } else {
      var title = columns.get(2).text();
      var album = columns.get(3).text();
      return BroadcastDto.builder()
          .time(localTime)
          .artist(secondField)
          .title(title)
          .album(album)
          .build();
    }
  }

}
