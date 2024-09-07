package net.lecigne.somafm.client;

import java.io.IOException;
import java.util.List;
import lombok.AllArgsConstructor;
import net.lecigne.somafm.client.dto.BroadcastDto;
import net.lecigne.somafm.client.dto.RecentBroadcastsDto;
import net.lecigne.somafm.config.SomaFmConfig;
import net.lecigne.somafm.model.Channel;
import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Get the most recent broadcasts from a given SomaFM channel.
 */
@AllArgsConstructor
public class RecentBroadcastsClient {

  private HtmlBroadcastsClient htmlBroadcastsClient;
  private HtmlBroadcastsParser htmlBroadcastsParser;

  public RecentBroadcastsDto get(Channel channel) throws IOException {
    Response<ResponseBody> response = htmlBroadcastsClient.getHtml(channel.getInternalName()).execute();
    List<BroadcastDto> recentBroadcasts = htmlBroadcastsParser.parse(response.body().string());
    return RecentBroadcastsDto.builder()
        .channel(channel)
        .recentBroadcasts(recentBroadcasts)
        .build();
  }

  public static RecentBroadcastsClient init(SomaFmConfig config) {
    return new RecentBroadcastsClient(HtmlBroadcastsClient.create(config), new HtmlBroadcastsParser());
  }

}
