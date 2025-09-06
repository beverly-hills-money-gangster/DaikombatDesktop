package com.beverly.hills.money.gang.factory;

import com.badlogic.gdx.math.Vector2;
import com.beverly.hills.money.gang.assets.managers.DaiKombatAssetsManager;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.TexturesRegistry;
import com.beverly.hills.money.gang.screens.ui.selection.GamePlayerClass;
import com.beverly.hills.money.gang.screens.ui.weapon.WeaponState;
import com.beverly.hills.money.gang.screens.ui.weapon.WeaponStats;
import java.util.Map;

public class PlasmagunWeaponStateFactory extends ScreenWeaponStateFactory {

  private static final int PLASMAGUN_ANIMATION_MLS = 150;

  private static final int TEXTURE_WIDTH = 188;
  private static final int TEXTURE_HEIGHT = 117;

  @Override
  public WeaponState create(DaiKombatAssetsManager assetsManager, WeaponStats weaponStats) {

    // TODO refactor it
    var warriorFireTexture = assetsManager.getTextureRegion(TexturesRegistry.PLASMAGUN_FIRE, 0, 0,
        TEXTURE_WIDTH, TEXTURE_HEIGHT);
    var skeletonFireTexture = assetsManager.getTextureRegion(
        TexturesRegistry.SKELETON_PLASMAGUN_FIRE,
        0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    var warriorIdleTexture = assetsManager.getTextureRegion(TexturesRegistry.PLASMAGUN_IDLE, 0, 0,
        TEXTURE_WIDTH, TEXTURE_HEIGHT);
    var skeletonIdleTexture = assetsManager.getTextureRegion(
        TexturesRegistry.SKELETON_PLASMAGUN_IDLE,
        0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT);

    return WeaponState.builder()
        .distance(weaponStats.getMaxDistance())
        .fireSound(assetsManager.getUserSettingSound(
            SoundRegistry.PLAYER_PLASMAGUN_FIRE))
        .hitTargetSound(assetsManager.getUserSettingSound(SoundRegistry.HIT_SOUND))
        .projectileRadius(weaponStats.getProjectileRadius())
        .screenRatioX(0.40f)
        .screenRatioY(0.45f)
        .maxAmmo(weaponStats.getMaxAmmo())
        .center(false)
        .backoffDelayMls(getBackoffDelay(weaponStats, PLASMAGUN_ANIMATION_MLS))
        .animationDelayMls(PLASMAGUN_ANIMATION_MLS)
        .fireTextures(Map.of(GamePlayerClass.WARRIOR, warriorFireTexture,
            GamePlayerClass.DEMON_TANK, warriorFireTexture,
            GamePlayerClass.ANGRY_SKELETON, skeletonFireTexture))
        .idleTextures(Map.of(GamePlayerClass.WARRIOR, warriorIdleTexture,
            GamePlayerClass.DEMON_TANK, warriorIdleTexture,
            GamePlayerClass.ANGRY_SKELETON, skeletonIdleTexture))
        .weaponScreenPositioning(
            animationTime -> new Vector2(0, -PLASMAGUN_ANIMATION_MLS + animationTime))
        .build();
  }
}
