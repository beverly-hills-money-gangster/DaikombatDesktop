package com.beverly.hills.money.gang.registry;

import com.beverly.hills.money.gang.factory.PlasmaProjectileFactory;
import com.beverly.hills.money.gang.factory.ProjectileFactory;
import com.beverly.hills.money.gang.factory.RocketProjectileFactory;
import com.beverly.hills.money.gang.screens.ui.weapon.WeaponProjectile;
import java.util.HashMap;
import java.util.Map;

public class PlayerProjectileFactoriesRegistry {

  private final Map<WeaponProjectile, ProjectileFactory> factories = new HashMap<>();

  {
    for (WeaponProjectile value : WeaponProjectile.values()) {
      var factory = switch (value) {
        case ROCKET -> new RocketProjectileFactory();
        case PLASMA ->  new PlasmaProjectileFactory();
      };
      factories.put(value, factory);
    }
  }

  public ProjectileFactory get(WeaponProjectile projectile) {
    return factories.get(projectile);
  }

}
