package com.beverly.hills.money.gang.screens.ui.selection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum GamePlayerClass {
  WARRIOR("Default stats"),
  ANGRY_SKELETON("Low defense, increased damage and speed"),
  DEMON_TANK("Increased defense, low vampirism and speed");

  @Getter
  private final String description;

  @Override
  public String toString() {
    return this.name().replace("_", " ");
  }

  public float getVoicePitch() {
    return switch (this) {
      case WARRIOR -> 1.125f;
      case DEMON_TANK -> 0.965f;
      case ANGRY_SKELETON -> 1.385f;
    };
  }

}
