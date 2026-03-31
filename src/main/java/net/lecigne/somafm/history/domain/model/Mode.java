package net.lecigne.somafm.history.domain.model;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Mode {
  API("api"), DISPLAY("display"), SAVE("save");

  private final String actionName;

  public static Mode getValue(String modeName) {
    return Arrays.stream(Mode.values())
        .filter(value -> value.actionName.equals(modeName))
        .findFirst()
        .orElse(DISPLAY);
  }

  public boolean needsDatabase() {
    return this == SAVE || this == API;
  }

}
