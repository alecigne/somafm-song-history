package net.lecigne.somafm.application.spi;

import java.util.List;
import net.lecigne.somafm.recentlib.Broadcast;
import net.lecigne.somafm.recentlib.Channel;

public interface SomaFmSongHistorySpi {
  List<Broadcast> getRecentBroadcasts(Channel channel);
  void updateBroadcasts(List<Broadcast> broadcasts);
}
