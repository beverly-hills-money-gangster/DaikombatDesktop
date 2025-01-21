package com.beverly.hills.money.gang.registry;

import com.beverly.hills.money.gang.factory.EnemyPlasmaProjectileShootFactory;
import com.beverly.hills.money.gang.factory.EnemyProjectileShootFactory;
import com.beverly.hills.money.gang.factory.EnemyRocketProjectileShootFactory;
import com.beverly.hills.money.gang.screens.ui.weapon.WeaponProjectile;
import java.util.HashMap;
import java.util.Map;

public class EnemyPlayerProjectileShootingFactoriesRegistry {

  private final Map<WeaponProjectile, EnemyProjectileShootFactory> factories = new HashMap<>();

  {
    for (WeaponProjectile value : WeaponProjectile.values()) {
      var factory = switch (value) {
        case ROCKET -> new EnemyRocketProjectileShootFactory();
        case PLASMA -> new EnemyPlasmaProjectileShootFactory();
      };
      factories.put(value, factory);
    }
  }

  public EnemyProjectileShootFactory get(WeaponProjectile projectile) {
    return factories.get(projectile);
  }

}
