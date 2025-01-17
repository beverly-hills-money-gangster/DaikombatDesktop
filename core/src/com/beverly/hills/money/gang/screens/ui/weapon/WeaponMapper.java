package com.beverly.hills.money.gang.screens.ui.weapon;

import com.beverly.hills.money.gang.proto.ProjectileType;
import com.beverly.hills.money.gang.proto.WeaponType;

public interface WeaponMapper {

  static Weapon getWeapon(WeaponType weaponType) {
    return switch (weaponType) {
      case PUNCH -> Weapon.GAUNTLET;
      case SHOTGUN -> Weapon.SHOTGUN;
      case RAILGUN -> Weapon.RAILGUN;
      case MINIGUN -> Weapon.MINIGUN;
      case ROCKET_LAUNCHER -> Weapon.ROCKET_LAUNCHER;
      case PLASMAGUN -> Weapon.PLASMAGUN;
      default -> throw new IllegalArgumentException("Not supported weapon type");
    };
  }

  static WeaponProjectile getWeaponProjectile(ProjectileType projectileType) {
    return switch (projectileType) {
      case ROCKET -> WeaponProjectile.ROCKET;
      case PLASMA -> WeaponProjectile.PLASMA;
      default -> throw new IllegalArgumentException("Not supported projectile type");
    };
  }
}
