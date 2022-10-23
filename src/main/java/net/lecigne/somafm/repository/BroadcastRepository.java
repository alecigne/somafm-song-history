package net.lecigne.somafm.repository;

import java.io.IOException;
import java.util.Set;
import net.lecigne.somafm.model.Broadcast;
import net.lecigne.somafm.model.Channel;

public interface BroadcastRepository {
  Set<Broadcast> getRecentBroadcasts(Channel channel) throws IOException;
}
