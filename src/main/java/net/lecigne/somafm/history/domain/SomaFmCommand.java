package net.lecigne.somafm.history.domain;

import net.lecigne.somafm.recentlib.Channel;

public record SomaFmCommand(Action action, Channel channel) {
}
