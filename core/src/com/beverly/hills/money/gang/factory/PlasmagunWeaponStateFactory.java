package com.beverly.hills.money.gang.factory;

import com.badlogic.gdx.math.Vector2;
import com.beverly.hills.money.gang.assets.managers.DaiKombatAssetsManager;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.TexturesRegistry;
import com.beverly.hills.money.gang.screens.ui.weapon.WeaponState;
import com.beverly.hills.money.gang.screens.ui.weapon.WeaponStats;

public class PlasmagunWeaponStateFactory extends ScreenWeaponStateFactory {

  private static final int PLASMAGUN_ANIMATION_MLS = 150; // TODO make delay 150 on the server side

  @Override
  public WeaponState create(DaiKombatAssetsManager assetsManager, WeaponStats weaponStats) {
    return WeaponState.builder()
        .distance(weaponStats.getMaxDistance())
        .fireSound(assetsManager.getUserSettingSound(
            SoundRegistry.PLAYER_PLASMAGUN_FIRE)) // TODO use normal sound
        .hitTargetSound(assetsManager.getUserSettingSound(SoundRegistry.HIT_SOUND))
        .projectileRadius(weaponStats.getProjectileRadius())
        .screenRatioX(0.40f)
        .screenRatioY(0.45f)
        .center(false)
        .backoffDelayMls(getBackoffDelay(weaponStats, PLASMAGUN_ANIMATION_MLS))
        .animationDelayMls(PLASMAGUN_ANIMATION_MLS)
        // TODO the texture is transparent in some places
        .fireTexture(assetsManager.getTextureRegion(TexturesRegistry.PLASMAGUN_FIRE, 0, 0,
            188, 117))
        .idleTexture(assetsManager.getTextureRegion(TexturesRegistry.PLASMAGUN_IDLE, 0, 0,
            188, 117))
        .weaponScreenPositioning(
            animationTime -> new Vector2(0, -PLASMAGUN_ANIMATION_MLS + animationTime))
        .build();
  }
}
