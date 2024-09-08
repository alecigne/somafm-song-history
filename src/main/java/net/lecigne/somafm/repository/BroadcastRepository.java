package net.lecigne.somafm.repository;

import java.io.IOException;
import java.util.List;
import net.lecigne.somafm.recentlib.Broadcast;
import net.lecigne.somafm.recentlib.Channel;

public interface BroadcastRepository {
  List<Broadcast> getRecentBroadcasts(Channel channel) throws IOException;
  void updateBroadcasts(List<Broadcast> broadcasts);
}
