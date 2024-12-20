package com.beverly.hills.money.gang.factory;

import com.badlogic.gdx.math.Vector2;
import com.beverly.hills.money.gang.assets.managers.DaiKombatAssetsManager;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.TexturesRegistry;
import com.beverly.hills.money.gang.screens.ui.weapon.WeaponState;
import com.beverly.hills.money.gang.screens.ui.weapon.WeaponStats;

public class MinigunWeaponStateFactory extends ScreenWeaponStateFactory {

  private static final int MINIGUN_ANIMATION_MLS = 100;

  @Override
  public WeaponState create(DaiKombatAssetsManager assetsManager, WeaponStats weaponStats) {
    return WeaponState.builder()
        .distance(weaponStats.getMaxDistance())
        .hitTargetSound(assetsManager.getUserSettingSound(SoundRegistry.HIT_SOUND))
        .fireSound(assetsManager.getUserSettingSound(SoundRegistry.PLAYER_MINIGUN))
        .screenRatioX(0.45f)
        .screenRatioY(0.40f)
        .backoffDelayMls(getBackoffDelay(weaponStats, MINIGUN_ANIMATION_MLS))
        .animationDelayMls(MINIGUN_ANIMATION_MLS)
        .center(true)
        .fireTexture(assetsManager.getTextureRegion(TexturesRegistry.MINIGUN_FIRE, 0, 0,
            185, 96))
        .idleTexture(assetsManager.getTextureRegion(TexturesRegistry.MINIGUN_IDLE, 0, 0,
            185, 96))
        .weaponScreenPositioning(
            animationTime -> new Vector2((float) Math.sin(animationTime) * 15f, -35))
        .build();
  }
}
