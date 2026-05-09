package net.lecigne.somafm.history.application.ports.in;

import java.util.List;
import net.lecigne.somafm.history.application.model.SomaFmCommand;
import net.lecigne.somafm.history.domain.model.Broadcast;

public interface RunCommandUseCase {
  List<Broadcast> runCommand(SomaFmCommand command);
}
