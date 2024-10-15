package com.beverly.hills.money.gang.screens.ui.weapon;

import com.beverly.hills.money.gang.proto.WeaponType;

public interface WeaponMapper {

  static Weapon getWeapon(WeaponType weaponType) {
    return switch (weaponType) {
      case PUNCH -> Weapon.GAUNTLET;
      case SHOTGUN -> Weapon.SHOTGUN;
      case RAILGUN -> Weapon.RAILGUN;
      case MINIGUN -> Weapon.MINIGUN;
      default -> throw new IllegalStateException("Not supported weapon " + weaponType);
    };
  }
}
