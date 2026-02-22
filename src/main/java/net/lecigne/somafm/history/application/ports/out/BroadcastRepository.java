package net.lecigne.somafm.history.application.ports.out;

import java.util.List;
import net.lecigne.somafm.recentlib.Broadcast;

public interface BroadcastRepository {
  long countBroadcasts();
  List<Broadcast> getBroadcasts(int page, int size);
  void updateBroadcasts(List<Broadcast> broadcasts);
}
