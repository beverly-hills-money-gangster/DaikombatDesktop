package com.beverly.hills.money.gang.factory;

import com.badlogic.gdx.math.Vector2;
import com.beverly.hills.money.gang.assets.managers.DaiKombatAssetsManager;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.TexturesRegistry;
import com.beverly.hills.money.gang.screens.ui.selection.GamePlayerClass;
import com.beverly.hills.money.gang.screens.ui.weapon.WeaponState;
import com.beverly.hills.money.gang.screens.ui.weapon.WeaponStats;
import java.util.Map;

public class RocketLauncherWeaponStateFactory extends ScreenWeaponStateFactory {

  private static final int TEXTURE_WIDTH = 217;
  private static final int TEXTURE_HEIGHT = 105;


  @Override
  public WeaponState create(DaiKombatAssetsManager assetsManager, WeaponStats weaponStats) {

    // TODO refactor it

    var warriorIdleTexture = assetsManager.getTextureRegion(TexturesRegistry.ROCKET_LAUNCHER_IDLE,
        0, 0,
        TEXTURE_WIDTH, TEXTURE_HEIGHT);
    var demonIdleTexture = assetsManager.getTextureRegion(
        TexturesRegistry.DEMON_ROCKET_LAUNCHER_IDLE,
        0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT);

    return WeaponState.builder()
        .distance(weaponStats.getMaxDistance())
        .projectileRadius(weaponStats.getProjectileRadius())
        .fireSound(assetsManager.getUserSettingSound(SoundRegistry.PLAYER_ROCKET_LAUNCHER))
        .hitTargetSound(assetsManager.getUserSettingSound(SoundRegistry.HIT_SOUND))
        .screenRatioX(0.50f)
        .screenRatioY(0.40f)
        .maxAmmo(weaponStats.getMaxAmmo())
        .center(false)
        .backoffDelayMls(0)
        .animationDelayMls(weaponStats.getDelayMls())
        .fireTextures(Map.of(GamePlayerClass.WARRIOR, warriorIdleTexture,
            GamePlayerClass.DEMON_TANK, demonIdleTexture,
            GamePlayerClass.ANGRY_SKELETON, warriorIdleTexture))
        .idleTextures(Map.of(GamePlayerClass.WARRIOR, warriorIdleTexture,
            GamePlayerClass.DEMON_TANK, demonIdleTexture,
            GamePlayerClass.ANGRY_SKELETON, warriorIdleTexture))
        .weaponScreenPositioning(
            animationTime -> new Vector2(0,
                Math.max(-350, -weaponStats.getDelayMls() + animationTime)))
        .build();
  }
}
