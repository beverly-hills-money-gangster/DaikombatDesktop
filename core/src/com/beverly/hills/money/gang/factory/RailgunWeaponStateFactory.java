package com.beverly.hills.money.gang.factory;

import com.badlogic.gdx.math.Vector2;
import com.beverly.hills.money.gang.assets.managers.DaiKombatAssetsManager;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.TexturesRegistry;
import com.beverly.hills.money.gang.screens.ui.weapon.WeaponState;
import com.beverly.hills.money.gang.screens.ui.weapon.WeaponStats;

public class RailgunWeaponStateFactory extends ScreenWeaponStateFactory {

  private static final int RAILGUN_ANIMATION_MLS = 200;

  @Override
  public WeaponState create(DaiKombatAssetsManager assetsManager, WeaponStats weaponStats) {
    return WeaponState.builder()
        .distance(weaponStats.getMaxDistance())
        .hitTargetSound(assetsManager.getUserSettingSound(SoundRegistry.HIT_SOUND))
        .fireSound(assetsManager.getUserSettingSound(SoundRegistry.PLAYER_RAILGUN))
        .screenRatioX(0.35f)
        .screenRatioY(0.40f)
        .center(false)
        .backoffDelayMls(getBackoffDelay(weaponStats, RAILGUN_ANIMATION_MLS))
        .animationDelayMls(RAILGUN_ANIMATION_MLS)
        .fireTexture(assetsManager.getTextureRegion(TexturesRegistry.RAILGUN_SHOOTING, 0, 0,
            170, 118))
        .idleTexture(assetsManager.getTextureRegion(TexturesRegistry.RAILGUN_IDLE, 0, 0,
            170, 118))
        .weaponScreenPositioning(
            animationTime -> new Vector2(0, -RAILGUN_ANIMATION_MLS + animationTime))
        .build();
  }
}
