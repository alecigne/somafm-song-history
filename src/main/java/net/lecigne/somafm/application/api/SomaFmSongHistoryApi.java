package net.lecigne.somafm.application.api;

import java.util.List;
import net.lecigne.somafm.recentlib.Broadcast;
import net.lecigne.somafm.recentlib.Channel;

public interface SomaFmSongHistoryApi {
  List<Broadcast> fetchRecentBroadcasts(Channel channel);
  List<Broadcast> saveRecentBroadcasts(Channel channel);
}
