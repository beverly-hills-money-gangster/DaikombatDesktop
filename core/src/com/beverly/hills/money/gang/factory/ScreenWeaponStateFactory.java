package com.beverly.hills.money.gang.factory;

import com.beverly.hills.money.gang.assets.managers.DaiKombatAssetsManager;
import com.beverly.hills.money.gang.screens.ui.weapon.WeaponState;
import com.beverly.hills.money.gang.screens.ui.weapon.WeaponStats;

public abstract class ScreenWeaponStateFactory {

  public abstract WeaponState create(DaiKombatAssetsManager assetsManager, WeaponStats weaponStats);

  protected static int getBackoffDelay(WeaponStats weaponStats, int animationMls) {
    return Math.max(0, weaponStats.getDelayMls() - animationMls);
  }
}
