package com.beverly.hills.money.gang.screens.ui.skin;

import com.beverly.hills.money.gang.entities.enemies.EnemyTextures;
import com.beverly.hills.money.gang.screens.GameScreen;

public class SkinSelectAnimation {

  private static final int STEP_ANIMATION_DELAY_MLS = 150;
  private EnemyTextures enemyTextures;
  private final GameScreen gameScreen;
  private int skinStep;
  private long stepUntilMls;

  private final int animationY;

  public SkinSelectAnimation(EnemyTextures enemyTextures, GameScreen gameScreen) {
    this.enemyTextures = enemyTextures;
    this.gameScreen = gameScreen;
    this.animationY = (int) (gameScreen.getViewport().getWorldHeight() / 2.5f);
  }

  public final int getAnimationYOffset() {
    return animationY - 16;
  }

  public void setEnemyTextures(EnemyTextures enemyTextures) {
    this.enemyTextures = enemyTextures;
  }

  public void render() {
    var texture = enemyTextures.getEnemyPlayerMoveFrontTextureRegion(skinStep);
    float scale = (gameScreen.getViewport().getWorldWidth() / 5f) / texture.getRegionWidth();
    gameScreen.getGame().getBatch().draw(texture,
        gameScreen.getViewport().getWorldWidth() / 2 - (texture.getRegionWidth() * scale) / 2f,
        animationY, texture.getRegionWidth() * scale, texture.getRegionHeight() * scale);

    if (System.currentTimeMillis() > stepUntilMls) {
      stepUntilMls = System.currentTimeMillis() + STEP_ANIMATION_DELAY_MLS;
      skinStep = (skinStep + 1) % 3;
    }
  }

}
