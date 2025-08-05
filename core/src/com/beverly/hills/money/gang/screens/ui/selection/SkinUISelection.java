package com.beverly.hills.money.gang.screens.ui.selection;

import com.beverly.hills.money.gang.proto.PlayerSkinColor;

public enum SkinUISelection {
  GREEN, PINK, PURPLE, BLUE, YELLOW, ORANGE;


  @Override
  public String toString() {
    return this.name();
  }

  public static SkinUISelection getSkinColor(
      PlayerSkinColor playerSkinColor) {
    return switch (playerSkinColor) {
      case BLUE -> SkinUISelection.BLUE;
      case PURPLE -> SkinUISelection.PURPLE;
      case PINK -> SkinUISelection.PINK;
      case GREEN -> SkinUISelection.GREEN;
      case ORANGE -> SkinUISelection.ORANGE;
      case YELLOW -> SkinUISelection.YELLOW;
      case UNRECOGNIZED ->  throw new IllegalStateException("Not supported skin color " + playerSkinColor);
    };
  }

}
