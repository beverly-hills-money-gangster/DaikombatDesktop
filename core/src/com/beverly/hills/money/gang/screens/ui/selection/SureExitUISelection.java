package com.beverly.hills.money.gang.screens.ui.selection;

import lombok.Getter;

public enum SureExitUISelection {
  YES("YES, QUIT"), NO("NO, KEEP GAMING");

  @Getter
  private final String title;

  SureExitUISelection(String title) {
    this.title = title;
  }


  @Override
  public String toString() {
    return this.title;
  }

}
