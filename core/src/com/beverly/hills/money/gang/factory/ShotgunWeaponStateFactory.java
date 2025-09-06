package com.beverly.hills.money.gang.factory;

import com.badlogic.gdx.math.Vector2;
import com.beverly.hills.money.gang.assets.managers.DaiKombatAssetsManager;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.TexturesRegistry;
import com.beverly.hills.money.gang.screens.ui.weapon.WeaponState;
import com.beverly.hills.money.gang.screens.ui.weapon.WeaponStats;

public class ShotgunWeaponStateFactory extends ScreenWeaponStateFactory {

  private static final int GUNSHOT_ANIMATION_MLS = 150;

  @Override
  public WeaponState create(DaiKombatAssetsManager assetsManager, WeaponStats weaponStats) {
    System.out.println(assetsManager);
    return WeaponState.builder()
        .distance(weaponStats.getMaxDistance())
        .fireSound(assetsManager.getUserSettingSound(SoundRegistry.PLAYER_SHOTGUN))
        .hitTargetSound(assetsManager.getUserSettingSound(SoundRegistry.HIT_SOUND))
        .screenRatioX(0.35f)
        .maxAmmo(weaponStats.getMaxAmmo())
        .screenRatioY(0.40f)
        .center(false)
        .backoffDelayMls(getBackoffDelay(weaponStats, GUNSHOT_ANIMATION_MLS))
        .animationDelayMls(GUNSHOT_ANIMATION_MLS)
        .fireTextures(sameTextureForAllClasses(
            assetsManager.getTextureRegion(TexturesRegistry.GUN_SHOOT, 0, 0,
                149, 117 - 10)))
        .idleTextures(
            sameTextureForAllClasses(assetsManager.getTextureRegion(TexturesRegistry.GUN_IDLE, 0, 0,
                149, 117 - 10)))
        .weaponScreenPositioning(
            animationTime -> new Vector2(0, -GUNSHOT_ANIMATION_MLS + animationTime))
        .build();
  }
}
