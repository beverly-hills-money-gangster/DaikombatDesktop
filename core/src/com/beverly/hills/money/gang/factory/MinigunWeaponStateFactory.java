package com.beverly.hills.money.gang.factory;

import com.badlogic.gdx.math.Vector2;
import com.beverly.hills.money.gang.assets.managers.DaiKombatAssetsManager;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.TexturesRegistry;
import com.beverly.hills.money.gang.screens.ui.selection.GamePlayerClass;
import com.beverly.hills.money.gang.screens.ui.weapon.WeaponState;
import com.beverly.hills.money.gang.screens.ui.weapon.WeaponStats;
import java.util.Map;

public class MinigunWeaponStateFactory extends ScreenWeaponStateFactory {

  private static final int MINIGUN_ANIMATION_MLS = 100;

  private static final int TEXTURE_WIDTH = 185;
  private static final int TEXTURE_HEIGHT = 96;

  @Override
  public WeaponState create(DaiKombatAssetsManager assetsManager, WeaponStats weaponStats) {

    var warriorFireTexture = assetsManager.getTextureRegion(TexturesRegistry.MINIGUN_FIRE,
        0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    var demonFireTexture = assetsManager.getTextureRegion(
        TexturesRegistry.DEMON_MINIGUN_FIRE, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT);

    var warriorIdleTexture = assetsManager.getTextureRegion(TexturesRegistry.MINIGUN_IDLE,
        0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    var demonIdleTexture = assetsManager.getTextureRegion(
        TexturesRegistry.DEMON_MINIGUN_IDLE, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT);

    return WeaponState.builder()
        .distance(weaponStats.getMaxDistance())
        .hitTargetSound(assetsManager.getUserSettingSound(SoundRegistry.HIT_SOUND))
        .fireSound(assetsManager.getUserSettingSound(SoundRegistry.PLAYER_MINIGUN))
        .screenRatioX(0.45f)
        .screenRatioY(0.40f)
        .maxAmmo(weaponStats.getMaxAmmo())
        .backoffDelayMls(getBackoffDelay(weaponStats, MINIGUN_ANIMATION_MLS))
        .animationDelayMls(MINIGUN_ANIMATION_MLS)
        .center(true)
        .fireTextures(Map.of(GamePlayerClass.WARRIOR, warriorFireTexture,
            GamePlayerClass.DEMON_TANK, demonFireTexture,
            GamePlayerClass.ANGRY_SKELETON, warriorFireTexture))
        .idleTextures(Map.of(GamePlayerClass.WARRIOR, warriorIdleTexture,
            GamePlayerClass.DEMON_TANK, demonIdleTexture,
            GamePlayerClass.ANGRY_SKELETON, warriorIdleTexture))
        .weaponScreenPositioning(
            animationTime -> new Vector2((float) Math.sin(animationTime) * 15f, -35))
        .build();
  }
}
