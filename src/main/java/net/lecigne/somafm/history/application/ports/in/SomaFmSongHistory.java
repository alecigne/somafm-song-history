package net.lecigne.somafm.history.application.ports.in;

import java.util.List;
import net.lecigne.somafm.history.domain.SomaFmCommand;
import net.lecigne.somafm.recentlib.Broadcast;

public interface SomaFmSongHistory {
  List<Broadcast> run(SomaFmCommand command);
}
