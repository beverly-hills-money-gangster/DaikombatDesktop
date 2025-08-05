package com.beverly.hills.money.gang.factory;

import com.badlogic.gdx.math.Vector2;
import com.beverly.hills.money.gang.assets.managers.DaiKombatAssetsManager;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.TexturesRegistry;
import com.beverly.hills.money.gang.screens.ui.selection.GamePlayerClass;
import com.beverly.hills.money.gang.screens.ui.weapon.WeaponState;
import com.beverly.hills.money.gang.screens.ui.weapon.WeaponStats;
import java.util.Map;

public class RailgunWeaponStateFactory extends ScreenWeaponStateFactory {

  private static final int RAILGUN_ANIMATION_MLS = 200;

  private static final int TEXTURE_WIDTH = 170;
  private static final int TEXTURE_HEIGHT = 118;

  @Override
  public WeaponState create(DaiKombatAssetsManager assetsManager, WeaponStats weaponStats) {

    var warriorFireTexture = assetsManager.getTextureRegion(TexturesRegistry.RAILGUN_SHOOTING, 0, 0,
        TEXTURE_WIDTH, TEXTURE_HEIGHT);
    var skeletonFireTexture = assetsManager.getTextureRegion(
        TexturesRegistry.SKELETON_RAILGUN_SHOOTING,
        0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    var warriorIdleTexture = assetsManager.getTextureRegion(TexturesRegistry.RAILGUN_IDLE, 0, 0,
        TEXTURE_WIDTH, TEXTURE_HEIGHT);
    var skeletonIdleTexture = assetsManager.getTextureRegion(TexturesRegistry.SKELETON_RAILGUN_IDLE,
        0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT);

    return WeaponState.builder()
        .distance(weaponStats.getMaxDistance())
        .hitTargetSound(assetsManager.getUserSettingSound(SoundRegistry.HIT_SOUND))
        .fireSound(assetsManager.getUserSettingSound(SoundRegistry.PLAYER_RAILGUN))
        .screenRatioX(0.35f)
        .screenRatioY(0.40f)
        .maxAmmo(weaponStats.getMaxAmmo())
        .center(false)
        .backoffDelayMls(getBackoffDelay(weaponStats, RAILGUN_ANIMATION_MLS))
        .animationDelayMls(RAILGUN_ANIMATION_MLS)
        .fireTextures(Map.of(GamePlayerClass.WARRIOR, warriorFireTexture,
            GamePlayerClass.DEMON_TANK, warriorFireTexture,
            GamePlayerClass.ANGRY_SKELETON, skeletonFireTexture))
        .idleTextures(Map.of(GamePlayerClass.WARRIOR, warriorIdleTexture,
            GamePlayerClass.DEMON_TANK, warriorIdleTexture,
            GamePlayerClass.ANGRY_SKELETON, skeletonIdleTexture))
        .weaponScreenPositioning(
            animationTime -> new Vector2(0, -RAILGUN_ANIMATION_MLS + animationTime))
        .build();
  }
}
