package com.beverly.hills.money.gang.factory;

import static com.beverly.hills.money.gang.configs.Constants.PUNCH_ANIMATION_MLS;

import com.badlogic.gdx.math.Vector2;
import com.beverly.hills.money.gang.assets.managers.DaiKombatAssetsManager;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.TexturesRegistry;
import com.beverly.hills.money.gang.screens.ui.selection.GamePlayerClass;
import com.beverly.hills.money.gang.screens.ui.weapon.WeaponState;
import com.beverly.hills.money.gang.screens.ui.weapon.WeaponStats;
import java.util.Map;

public class GauntletWeaponStateFactory extends ScreenWeaponStateFactory {

  private static final int TEXTURE_WIDTH = 273;
  private static final int TEXTURE_HEIGHT = 173;

  @Override
  public WeaponState create(DaiKombatAssetsManager assetsManager, WeaponStats weaponStats) {
    // TODO refactor it
    var warriorFireTexture = assetsManager.getTextureRegion(TexturesRegistry.PUNCH, 0, 0,
        TEXTURE_WIDTH, TEXTURE_HEIGHT);
    var demonFireTexture = assetsManager.getTextureRegion(TexturesRegistry.DEMON_PUNCH, 0, 0,
        TEXTURE_WIDTH, TEXTURE_HEIGHT);
    var skeletonFireTexture = assetsManager.getTextureRegion(TexturesRegistry.SKELETON_PUNCH,
        0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    var warriorIdleTexture = assetsManager.getTextureRegion(TexturesRegistry.PUNCH_IDLE, 0, 0,
        TEXTURE_WIDTH, TEXTURE_HEIGHT);
    var demonIdleTexture = assetsManager.getTextureRegion(TexturesRegistry.DEMON_PUNCH_IDLE,
        0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    var skeletonIdleTexture = assetsManager.getTextureRegion(TexturesRegistry.SKELETON_PUNCH_IDLE,
        0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT);

    return WeaponState.builder()
        .distance(weaponStats.getMaxDistance())
        .fireSound(assetsManager.getUserSettingSound(SoundRegistry.PUNCH_THROWN))
        .hitTargetSound(assetsManager.getUserSettingSound(SoundRegistry.PUNCH_HIT))
        .screenRatioX(0.5f)
        .screenRatioY(0.45f)
        .center(false)
        .backoffDelayMls(getBackoffDelay(weaponStats, PUNCH_ANIMATION_MLS))
        .animationDelayMls(PUNCH_ANIMATION_MLS)
        .fireTextures(Map.of(GamePlayerClass.WARRIOR, warriorFireTexture,
            GamePlayerClass.DEMON_TANK, demonFireTexture,
            GamePlayerClass.ANGRY_SKELETON, skeletonFireTexture))
        .idleTextures(Map.of(GamePlayerClass.WARRIOR, warriorIdleTexture,
            GamePlayerClass.DEMON_TANK, demonIdleTexture,
            GamePlayerClass.ANGRY_SKELETON, skeletonIdleTexture))
        .weaponScreenPositioning(
            animationTime -> new Vector2(-(animationTime / (float) PUNCH_ANIMATION_MLS) * 200 + 200,
                0))
        .build();
  }
}
