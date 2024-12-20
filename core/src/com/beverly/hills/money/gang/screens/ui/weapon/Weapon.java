package com.beverly.hills.money.gang.screens.ui.weapon;

import com.badlogic.gdx.Input.Keys;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

// TODO maybe refactor this?
@RequiredArgsConstructor
public enum Weapon {
  GAUNTLET(Keys.NUM_0, true, null),
  SHOTGUN(Keys.NUM_1, false, null),
  RAILGUN(Keys.NUM_2, false, null),
  MINIGUN(Keys.NUM_3, true, null),
  ROCKET_LAUNCHER(Keys.NUM_4, false, WeaponProjectile.ROCKET);

  private static final List<Weapon> ALL_HOLDABLE_WEAPONS = Arrays.stream(Weapon.values())
      .collect(Collectors.toList());

  static {
    int keys = ALL_HOLDABLE_WEAPONS.stream().map(weapon -> weapon.selectKeyCode)
        .collect(Collectors.toSet()).size();
    if (keys != ALL_HOLDABLE_WEAPONS.size()) {
      throw new IllegalStateException("Not all weapons have unique select key mapping");
    }
  }

  public static Weapon getWeaponForProjectile(final WeaponProjectile weaponProjectile) {
    return Arrays.stream(Weapon.values()).filter(
        weapon -> weapon.getProjectileRef() == weaponProjectile).findFirst().get();
  }

  // TODO remove it
  public static List<Weapon> getAllHoldableWeapons() {
    return ALL_HOLDABLE_WEAPONS;
  }

  @Getter
  private final Integer selectKeyCode;

  @Getter
  private final boolean automatic;


  @Getter
  private final WeaponProjectile projectileRef;

  public Weapon nextWeapon() {
    return Weapon.getAllHoldableWeapons().get((getCurrentIdx() + 1) %
        Weapon.getAllHoldableWeapons().size());
  }

  public boolean hasProjectile() {
    return projectileRef != null;
  }

  public Weapon prevWeapon() {
    int prevIndex = getCurrentIdx() - 1;
    if (prevIndex < 0) {
      prevIndex = Weapon.getAllHoldableWeapons().size() - 1;
    }
    return Weapon.getAllHoldableWeapons().get(prevIndex % Weapon.getAllHoldableWeapons().size());
  }

  private int getCurrentIdx() {
    for (int i = 0; i < Weapon.getAllHoldableWeapons().size(); i++) {
      if (this == Weapon.getAllHoldableWeapons().get(i)) {
        return i;
      }
    }
    throw new IllegalStateException("Can't find weapon index");
  }

}
