package net.lecigne.somafm.history.application.ports.in;

import net.lecigne.somafm.history.domain.model.Page;
import net.lecigne.somafm.recentlib.Broadcast;

public interface GetBroadcastsUseCase {
  Page<Broadcast> getBroadcasts(int page, int size);
}
