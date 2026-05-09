package net.lecigne.somafm.history.adapters.out;

import java.util.List;
import net.lecigne.somafm.history.application.ports.out.SomaFmRepository;
import net.lecigne.somafm.history.domain.model.Broadcast;
import net.lecigne.somafm.history.domain.model.Song;
import net.lecigne.somafm.recentlib.Channel;
import net.lecigne.somafm.recentlib.SomaFm;

/**
 * Repository for retrieving SomaFM broadcasts from HTML "recent" pages.
 * <p>
 * This repository uses somafm-recentlib.
 *
 * @see <a href="https://github.com/alecigne/somafm-recentlib">somafm-recentlib</a>
 */
public class HtmlSomaFmRepository implements SomaFmRepository {

  private final SomaFm somaFmClient;

  HtmlSomaFmRepository(SomaFm somaFmClient) {
    this.somaFmClient = somaFmClient;
  }

  /**
   * Fetch recent broadcasts from SomaFM.
   */
  public List<Broadcast> fetchRecentBroadcasts(Channel channel) {
    List<net.lecigne.somafm.recentlib.Broadcast> broadcastDtos = somaFmClient.fetchRecent(channel);
    return broadcastDtos.stream()
        .map(broadcastDto -> Broadcast.builder()
            .time(broadcastDto.time())
            .channel(broadcastDto.channel())
            .song(Song.builder()
                .artist(broadcastDto.song().artist() == null ? null : broadcastDto.song().artist().name())
                .title(broadcastDto.song().title())
                .album(broadcastDto.song().album())
                .build())
            .build())
        .toList();
  }

  public static SomaFmRepository init(SomaFm somaFmClient) {
    return new HtmlSomaFmRepository(somaFmClient);
  }

}
