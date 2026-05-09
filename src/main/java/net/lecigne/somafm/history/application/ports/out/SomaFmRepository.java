package net.lecigne.somafm.history.application.ports.out;

import java.util.List;
import net.lecigne.somafm.history.domain.model.Broadcast;
import net.lecigne.somafm.recentlib.Channel;

public interface SomaFmRepository {
  List<Broadcast> fetchRecentBroadcasts(Channel channel);
}
