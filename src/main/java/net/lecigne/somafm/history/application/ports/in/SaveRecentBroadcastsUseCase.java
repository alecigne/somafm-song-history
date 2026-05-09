package net.lecigne.somafm.history.application.ports.in;

import java.util.List;
import net.lecigne.somafm.history.domain.model.Broadcast;
import net.lecigne.somafm.recentlib.Channel;

public interface SaveRecentBroadcastsUseCase {
  List<Broadcast> saveRecent(Channel channel);
}
