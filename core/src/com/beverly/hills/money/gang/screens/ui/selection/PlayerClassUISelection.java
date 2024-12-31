package com.beverly.hills.money.gang.screens.ui.selection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PlayerClassUISelection {
  WARRIOR("Default stats"),
  ANGRY_SKELETON("Low defense, increased damage and gun speed"),
  DEMON_TANK("Increased defense, low vampirism");


  @Getter
  private final String description;

  @Override
  public String toString() {
    return this.name().replace("_", " ");
  }

}
