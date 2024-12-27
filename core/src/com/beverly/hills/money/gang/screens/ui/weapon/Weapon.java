package com.beverly.hills.money.gang.screens.ui.weapon;

import com.badlogic.gdx.Input.Keys;
import java.util.Arrays;
import java.util.function.Predicate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Weapon {
  GAUNTLET(Keys.NUM_0, true, null),
  SHOTGUN(Keys.NUM_1, false, null),
  RAILGUN(Keys.NUM_2, false, null),
  MINIGUN(Keys.NUM_3, true, null),
  ROCKET_LAUNCHER(Keys.NUM_4, false, WeaponProjectile.ROCKET);


  @Getter
  private final int selectKeyCode;

  @Getter
  private final boolean automatic;


  @Getter
  private final WeaponProjectile projectileRef;

  public static Weapon getWeaponForProjectile(WeaponProjectile weaponProjectile) {
    return Arrays.stream(Weapon.values()).filter(
        weapon -> weapon.getProjectileRef() == weaponProjectile).findFirst().get();
  }

  public Weapon nextWeapon() {
    return Weapon.values()[(getCurrentIdx() + 1) % Weapon.values().length];
  }

  public boolean hasProjectile() {
    return projectileRef != null;
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
