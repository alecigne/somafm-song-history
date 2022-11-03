package net.lecigne.somafm.business;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum BusinessAction {
  DISPLAY("display"), SAVE("save");

  private final String actionName;

  public static BusinessAction getValue(String actionName) {
    return Arrays.stream(BusinessAction.values())
        .filter(value -> value.actionName.equals(actionName))
        .findFirst()
        .orElse(DISPLAY);
  }

}
