package net.lecigne.somafm.domain;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Action {
  DISPLAY("display"), SAVE("save");

  private final String actionName;

  public static Action getValue(String actionName) {
    return Arrays.stream(Action.values())
        .filter(value -> value.actionName.equals(actionName))
        .findFirst()
        .orElse(DISPLAY);
  }

}
