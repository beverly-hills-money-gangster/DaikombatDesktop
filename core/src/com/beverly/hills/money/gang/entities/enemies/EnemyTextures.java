package com.beverly.hills.money.gang.entities.enemies;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.beverly.hills.money.gang.assets.managers.DaiKombatAssetsManager;
import com.beverly.hills.money.gang.assets.managers.registry.SkinTextureTemplateRegistry;
import com.beverly.hills.money.gang.screens.ui.selection.PlayerClassUISelection;
import com.beverly.hills.money.gang.screens.ui.selection.SkinUISelection;
import java.util.HashMap;
import java.util.Map;

public class EnemyTextures {


  private final PlayerClassUISelection playerClassUISelection;

  private final SkinUISelection skinUISelection;

  private static final int ENEMY_PLAYER_SPRITE_HEIGHT = 66;
  private static final int ENEMY_PLAYER_SPRITE_WIDTH = 55;
  private final Map<EnemyTextureRegistry, TextureRegion> textures = new HashMap<>();

  private static final EnemyTextureRegistry[] FRONT_THREE_STEP = {
      EnemyTextureRegistry.MOVING1TEXFRONTREG, EnemyTextureRegistry.MOVING2TEXFRONTREG,
      EnemyTextureRegistry.MOVING3TEXFRONTREG};
  private static final EnemyTextureRegistry[] BACK_THREE_STEP = {
      EnemyTextureRegistry.MOVING1TEXBACKREG, EnemyTextureRegistry.MOVING2TEXBACKREG,
      EnemyTextureRegistry.MOVING3TEXBACKREG};
  private static final EnemyTextureRegistry[] LEFT_THREE_STEP = {
      EnemyTextureRegistry.MOVING1TEXLEFTREG, EnemyTextureRegistry.MOVING2TEXLEFTREG,
      EnemyTextureRegistry.MOVING3TEXLEFTREG};
  private static final EnemyTextureRegistry[] RIGHT_THREE_STEP = {
      EnemyTextureRegistry.MOVING1TEXRIGHTREG, EnemyTextureRegistry.MOVING2TEXRIGHTREG,
      EnemyTextureRegistry.MOVING3TEXRIGHTREG};

  private final DaiKombatAssetsManager assetsManager;

  public EnemyTextures(DaiKombatAssetsManager assetsManager,
      PlayerClassUISelection playerClassUISelection,
      SkinUISelection skinUISelection) {
    this.assetsManager = assetsManager;
    this.playerClassUISelection = playerClassUISelection;
    this.skinUISelection = skinUISelection;
    textures.put(EnemyTextureRegistry.IDLETEXFRONTREG, getEnemyPlayerTextureRegion(0));
    textures.put(EnemyTextureRegistry.MOVING1TEXFRONTREG, getEnemyPlayerTextureRegion(11));
    textures.put(EnemyTextureRegistry.MOVING2TEXFRONTREG, getEnemyPlayerTextureRegion(14));
    textures.put(EnemyTextureRegistry.MOVING3TEXFRONTREG, getEnemyPlayerTextureRegion(2));

    textures.put(EnemyTextureRegistry.IDLETEXBACKREG, getEnemyPlayerTextureRegion(1));
    textures.put(EnemyTextureRegistry.MOVING1TEXBACKREG, getEnemyPlayerTextureRegion(3));
    textures.put(EnemyTextureRegistry.MOVING2TEXBACKREG, getEnemyPlayerTextureRegion(15));
    textures.put(EnemyTextureRegistry.MOVING3TEXBACKREG, getEnemyPlayerTextureRegion(12));

    textures.put(EnemyTextureRegistry.IDLETEXLEFTREG, getEnemyPlayerTextureRegion(7));
    textures.put(EnemyTextureRegistry.MOVING1TEXLEFTREG, getEnemyPlayerTextureRegion(9));
    textures.put(EnemyTextureRegistry.MOVING2TEXLEFTREG, getEnemyPlayerTextureRegion(13));
    textures.put(EnemyTextureRegistry.MOVING3TEXLEFTREG, getEnemyPlayerTextureRegion(10));

    textures.put(EnemyTextureRegistry.IDLETEXRIGHTREG, getEnemyPlayerTextureRegion(7, true));
    textures.put(EnemyTextureRegistry.MOVING1TEXRIGHTREG, getEnemyPlayerTextureRegion(9, true));
    textures.put(EnemyTextureRegistry.MOVING2TEXRIGHTREG, getEnemyPlayerTextureRegion(13, true));
    textures.put(EnemyTextureRegistry.MOVING3TEXRIGHTREG, getEnemyPlayerTextureRegion(10, true));

    textures.put(EnemyTextureRegistry.SHOOTINGTEXRIGHTREG, getEnemyPlayerTextureRegion(8, true));
    textures.put(EnemyTextureRegistry.SHOOTINGTEXLEFTREG, getEnemyPlayerTextureRegion(8));
    textures.put(EnemyTextureRegistry.SHOOTINGTEXFRONTREG, getEnemyPlayerTextureRegion(4));
    textures.put(EnemyTextureRegistry.SHOOTINGTEXBACKTREG, getEnemyPlayerTextureRegion(5));

    textures.put(EnemyTextureRegistry.DEATHTEXREG, getEnemyPlayerTextureRegion(6));

    for (EnemyTextureRegistry enemyTextureRegistry : EnemyTextureRegistry.values()) {
      if (!textures.containsKey(enemyTextureRegistry)) {
        throw new IllegalStateException(
            "Texture " + enemyTextureRegistry.toString() + " is not loaded");
      }
    }
  }

  public TextureRegion getEnemyPlayerTextureRegion(EnemyTextureRegistry enemyTextureRegistry) {
    return textures.get(enemyTextureRegistry);
  }

  private TextureRegion getThreeStepAnimation(EnemyTextureRegistry[] animation, int step) {
    return getEnemyPlayerTextureRegion(animation[step % 3]);
  }

  public TextureRegion getEnemyPlayerMoveFrontTextureRegion(int step) {
    return getThreeStepAnimation(FRONT_THREE_STEP, step);
  }

  public TextureRegion getEnemyPlayerMoveBackTextureRegion(int step) {
    return getThreeStepAnimation(BACK_THREE_STEP, step);
  }


  public TextureRegion getEnemyPlayerMoveLeftTextureRegion(int step) {
    return getThreeStepAnimation(LEFT_THREE_STEP, step);
  }

  public TextureRegion getEnemyPlayerMoveRightTextureRegion(int step) {
    return getThreeStepAnimation(RIGHT_THREE_STEP, step);
  }

  private TextureRegion getEnemyPlayerTextureRegion(int spriteNumber) {
    return getEnemyPlayerTextureRegion(spriteNumber, false);
  }

  private TextureRegion getEnemyPlayerTextureRegion(int spriteNumber, boolean mirrorX) {
    TextureRegion region = assetsManager.getTextureRegion(
        SkinTextureTemplateRegistry.getTextureFileNameForClass(playerClassUISelection, skinUISelection),
        spriteNumber * ENEMY_PLAYER_SPRITE_WIDTH, 0, ENEMY_PLAYER_SPRITE_WIDTH,
        ENEMY_PLAYER_SPRITE_HEIGHT);
    if (!mirrorX) {
      region.flip(true, false);
    }
    return region;
  }


}
