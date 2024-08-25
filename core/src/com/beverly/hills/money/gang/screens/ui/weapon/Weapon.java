package com.beverly.hills.money.gang.screens.ui.weapon;

import com.badlogic.gdx.Input.Keys;
import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Weapon {
  GAUNTLET(Keys.NUM_1, true),
  SHOTGUN(Keys.NUM_2, false),
  RAILGUN(Keys.NUM_3, false),
  MINIGUN(Keys.NUM_4, true);

  static {
    int keys = Arrays.stream(Weapon.values()).map(weapon -> weapon.selectKeyCode)
        .collect(Collectors.toSet()).size();
    if (keys != Weapon.values().length) {
      throw new IllegalStateException("Not all weapons have unique select key mapping");
    }
  }

  @Getter
  private final int selectKeyCode;

  @Getter
  private final boolean automatic;

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
