package com.beverly.hills.money.gang.factory;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.beverly.hills.money.gang.assets.managers.DaiKombatAssetsManager;
import com.beverly.hills.money.gang.screens.ui.selection.GamePlayerClass;
import com.beverly.hills.money.gang.screens.ui.weapon.WeaponState;
import com.beverly.hills.money.gang.screens.ui.weapon.WeaponStats;
import java.util.HashMap;
import java.util.Map;

public abstract class ScreenWeaponStateFactory {

  public abstract WeaponState create(DaiKombatAssetsManager assetsManager, WeaponStats weaponStats);

  protected static int getBackoffDelay(WeaponStats weaponStats, int animationMls) {
    return Math.max(0, weaponStats.getDelayMls() - animationMls);
  }

  public static Map<GamePlayerClass, TextureRegion> sameTextureForAllClasses(
      TextureRegion textureRegion) {
    Map<GamePlayerClass, TextureRegion> textures = new HashMap<>();
    for (GamePlayerClass value : GamePlayerClass.values()) {
      textures.put(value, textureRegion);
    }
    return textures;
  }
}
