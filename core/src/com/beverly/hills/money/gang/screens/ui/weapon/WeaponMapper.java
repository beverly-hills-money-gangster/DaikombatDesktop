package com.beverly.hills.money.gang.screens.ui.weapon;

import com.beverly.hills.money.gang.proto.ServerResponse.GameEvent;

public interface WeaponMapper {

  static Weapon getWeapon(GameEvent.WeaponType weaponType) {
    return switch (weaponType) {
      case PUNCH -> Weapon.GAUNTLET;
      case SHOTGUN -> Weapon.SHOTGUN;
      case RAILGUN -> Weapon.RAILGUN;
      default -> throw new IllegalStateException("Not supported weapon " + weaponType);
    };
  }
}
