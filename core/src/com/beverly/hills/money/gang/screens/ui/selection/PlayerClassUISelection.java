package com.beverly.hills.money.gang.screens.ui.selection;

public enum PlayerClassUISelection {
  COMMONER, DRACULA_BERSERK, DEMON_TANK, BEAST_WARRIOR;


  @Override
  public String toString() {
    return this.name().replace("_", " ");
  }

}
