package net.lecigne.somafm.history.domain.model;

import java.time.Instant;
import lombok.Builder;
import net.lecigne.somafm.recentlib.Channel;

@Builder
public record Broadcast(Channel channel, Instant time, Song song) {
}
