package net.lecigne.somafm.history.application.services;

import java.util.List;
import net.lecigne.somafm.history.application.ports.in.FetchRecentBroadcastsUseCase;
import net.lecigne.somafm.history.application.ports.in.RunCommandUseCase;
import net.lecigne.somafm.history.application.ports.in.SaveRecentBroadcastsUseCase;
import net.lecigne.somafm.history.domain.model.SomaFmCommand;
import net.lecigne.somafm.recentlib.Broadcast;

public class SomaFmCommandDispatcher implements RunCommandUseCase {

  private final FetchRecentBroadcastsUseCase fetchRecentBroadcastsUseCase;
  private final SaveRecentBroadcastsUseCase saveRecentBroadcastsUseCase;

  SomaFmCommandDispatcher(
      FetchRecentBroadcastsUseCase fetchRecentBroadcastsUseCase,
      SaveRecentBroadcastsUseCase saveRecentBroadcastsUseCase) {
    this.fetchRecentBroadcastsUseCase = fetchRecentBroadcastsUseCase;
    this.saveRecentBroadcastsUseCase = saveRecentBroadcastsUseCase;
  }

  @Override
  public List<Broadcast> runCommand(SomaFmCommand command) {
    return switch (command.mode()) {
      case DISPLAY -> fetchRecentBroadcastsUseCase.fetchRecent(command.channel());
      case SAVE -> saveRecentBroadcastsUseCase.saveRecent(command.channel());
      case API -> throw new IllegalArgumentException();
    };
  }

  public static SomaFmCommandDispatcher init(
      FetchRecentBroadcastsUseCase fetchRecentBroadcastsUseCase,
      SaveRecentBroadcastsUseCase saveRecentBroadcastsUseCase) {
    return new SomaFmCommandDispatcher(fetchRecentBroadcastsUseCase, saveRecentBroadcastsUseCase);
  }

}
