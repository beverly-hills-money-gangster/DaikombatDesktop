package com.beverly.hills.money.gang.screens.ui.selection;

import lombok.Getter;

public enum GameOverUISelection {
  PLAY("PLAY AGAIN"), QUIT("MAIN MENU");

  @Getter
  private final String title;

  GameOverUISelection(String title) {
    this.title = title;
  }


  @Override
  public String toString() {
    return this.title;
  }

}
