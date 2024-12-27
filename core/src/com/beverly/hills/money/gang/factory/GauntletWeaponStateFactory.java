package com.beverly.hills.money.gang.factory;

import static com.beverly.hills.money.gang.Constants.PUNCH_ANIMATION_MLS;

import com.badlogic.gdx.math.Vector2;
import com.beverly.hills.money.gang.assets.managers.DaiKombatAssetsManager;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.TexturesRegistry;
import com.beverly.hills.money.gang.screens.ui.weapon.WeaponState;
import com.beverly.hills.money.gang.screens.ui.weapon.WeaponStats;

public class GauntletWeaponStateFactory extends ScreenWeaponStateFactory {

  @Override
  public WeaponState create(DaiKombatAssetsManager assetsManager, WeaponStats weaponStats) {
    return WeaponState.builder()
        .distance(weaponStats.getMaxDistance())
        .fireSound(assetsManager.getUserSettingSound(SoundRegistry.PUNCH_THROWN))
        .hitTargetSound(assetsManager.getUserSettingSound(SoundRegistry.PUNCH_HIT))
        .screenRatioX(0.5f)
        .screenRatioY(0.45f)
        .center(false)
        .backoffDelayMls(getBackoffDelay(weaponStats, PUNCH_ANIMATION_MLS))
        .animationDelayMls(PUNCH_ANIMATION_MLS)
        .fireTexture(assetsManager.getTextureRegion(TexturesRegistry.PUNCH, 0, 0,
            273, 175))
        .idleTexture(assetsManager.getTextureRegion(TexturesRegistry.PUNCH_IDLE, 0, 0,
            273, 175))
        .weaponScreenPositioning(
            animationTime -> new Vector2(-(animationTime / (float) PUNCH_ANIMATION_MLS) * 200 + 200,
                0))
        .build();
  }
}
