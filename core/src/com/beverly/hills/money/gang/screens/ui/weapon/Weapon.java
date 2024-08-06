package com.beverly.hills.money.gang.screens.ui.weapon;

public enum Weapon {
  GAUNTLET, SHOTGUN, RAILGUN;

  public Weapon nextWeapon() {
    return Weapon.values()[(getCurrentIdx() + 1) % Weapon.values().length];
  }

  public Weapon prevWeapon() {
    int prevIndex = getCurrentIdx() - 1;
    if (prevIndex < 0) {
      prevIndex = Weapon.values().length - 1;
    }
    return Weapon.values()[prevIndex % Weapon.values().length];
  }

  private int getCurrentIdx() {
    for (int i = 0; i < Weapon.values().length; i++) {
      if (this == Weapon.values()[i]) {
        return i;
      }
    }
    throw new IllegalStateException("Can't find weapon index");
  }

}
