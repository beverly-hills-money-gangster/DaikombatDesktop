package com.beverly.hills.money.gang.factory;

import com.badlogic.gdx.math.Vector2;
import com.beverly.hills.money.gang.assets.managers.DaiKombatAssetsManager;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.TexturesRegistry;
import com.beverly.hills.money.gang.screens.ui.weapon.WeaponState;
import com.beverly.hills.money.gang.screens.ui.weapon.WeaponStats;

public class RocketLauncherWeaponStateFactory extends ScreenWeaponStateFactory {

  private static final int ROCKET_LAUNCHER_ANIMATION_MLS = 200;

  @Override
  public WeaponState create(DaiKombatAssetsManager assetsManager, WeaponStats weaponStats) {
    return WeaponState.builder()
        .distance(weaponStats.getMaxDistance())
        .projectileRadius(weaponStats.getProjectileRadius())
        .fireSound(assetsManager.getUserSettingSound(SoundRegistry.PLAYER_ROCKET_LAUNCHER))
        .hitTargetSound(assetsManager.getUserSettingSound(SoundRegistry.HIT_SOUND))
        .screenRatioX(0.50f)
        .screenRatioY(0.40f)
        .center(false)
        .backoffDelayMls(
            getBackoffDelay(weaponStats, ROCKET_LAUNCHER_ANIMATION_MLS))
        .animationDelayMls(ROCKET_LAUNCHER_ANIMATION_MLS)
        .fireTexture(assetsManager.getTextureRegion(TexturesRegistry.ROCKET_LAUNCHER_SHOOTING, 0, 0,
            217, 105))
        .idleTexture(assetsManager.getTextureRegion(TexturesRegistry.ROCKET_LAUNCHER_IDLE, 0, 0,
            217, 105))
        .weaponScreenPositioning(
            animationTime -> new Vector2(0, -ROCKET_LAUNCHER_ANIMATION_MLS + animationTime))
        .build();
  }
}
