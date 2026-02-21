package net.lecigne.somafm.history.adapters.out;

import java.util.List;
import net.lecigne.somafm.history.application.ports.out.SomaFmRepository;
import net.lecigne.somafm.recentlib.Broadcast;
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
  public List<Broadcast> getRecentBroadcasts(Channel channel) {
    return somaFmClient.fetchRecent(channel);
  }

  public static SomaFmRepository init(SomaFm somaFmClient) {
    return new HtmlSomaFmRepository(somaFmClient);
  }

}
