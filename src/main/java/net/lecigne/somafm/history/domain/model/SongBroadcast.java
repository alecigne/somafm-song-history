package net.lecigne.somafm.history.domain.model;

import java.time.Instant;
import net.lecigne.somafm.recentlib.Channel;

public record SongBroadcast(Instant time, Channel channel) {
}
