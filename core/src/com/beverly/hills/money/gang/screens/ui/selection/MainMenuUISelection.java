package com.beverly.hills.money.gang.screens.ui.selection;

import lombok.Getter;

public enum MainMenuUISelection {
  PLAY("START GAME"), CONTROLS("CONTROLS"), SETTINGS("SETTINGS"), QUIT("QUIT GAME");

  @Getter
  private final String title;

  MainMenuUISelection(String title) {
    this.title = title;
  }


  @Override
  public String toString() {
    return this.title;
  }

}
