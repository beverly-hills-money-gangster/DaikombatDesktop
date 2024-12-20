package com.beverly.hills.money.gang.registry;

import com.beverly.hills.money.gang.factory.EnemyProjectileBoomFactory;
import com.beverly.hills.money.gang.factory.EnemyRocketProjectileBoomFactory;
import com.beverly.hills.money.gang.screens.ui.weapon.WeaponProjectile;
import java.util.HashMap;
import java.util.Map;

public class EnemyPlayerProjectileBoomFactoriesRegistry {

  private final Map<WeaponProjectile, EnemyProjectileBoomFactory> factories = new HashMap<>();

  {
    for (WeaponProjectile value : WeaponProjectile.values()) {
      var factory = switch (value) {
        case ROCKET -> new EnemyRocketProjectileBoomFactory();
      };
      factories.put(value, factory);
    }
  }

  public EnemyProjectileBoomFactory get(WeaponProjectile projectile) {
    return factories.get(projectile);
  }

}
