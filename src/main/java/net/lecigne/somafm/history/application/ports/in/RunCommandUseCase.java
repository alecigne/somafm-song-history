package net.lecigne.somafm.history.application.ports.in;

import java.util.List;
import net.lecigne.somafm.history.domain.model.SomaFmCommand;
import net.lecigne.somafm.recentlib.Broadcast;

public interface RunCommandUseCase {
  List<Broadcast> runCommand(SomaFmCommand command);
}
