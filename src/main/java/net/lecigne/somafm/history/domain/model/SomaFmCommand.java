package net.lecigne.somafm.history.domain.model;

import net.lecigne.somafm.recentlib.Channel;

public record SomaFmCommand(Mode mode, Channel channel) {
}
