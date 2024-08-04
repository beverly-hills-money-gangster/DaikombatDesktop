package com.beverly.hills.money.gang.animation;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.beverly.hills.money.gang.assets.managers.DaiKombatAssetsManager;
import com.beverly.hills.money.gang.assets.managers.registry.TexturesRegistry;
import lombok.Builder;

public class Animation {

  private final int height;

  private final int width;

  private final int animationStepMls;

  private final int animationSteps;

  private final TexturesRegistry texturesRegistry;

  private long switchAnimationMls;

  private int currentStep;

  @Builder
  public Animation(int height, int width, int animationStepMls, int animationSteps,
      TexturesRegistry texturesRegistry) {
    this.height = height;
    this.width = width;
    this.animationStepMls = animationStepMls;
    this.animationSteps = animationSteps;
    this.texturesRegistry = texturesRegistry;
    this.switchAnimationMls = System.currentTimeMillis() + animationStepMls;
  }

  public TextureRegion getCurrentTextureRegion(final DaiKombatAssetsManager assetsManager) {
    if (System.currentTimeMillis() > switchAnimationMls) {
      currentStep++;
      switchAnimationMls = System.currentTimeMillis() + animationStepMls;
    }
    return assetsManager
        .getTextureRegion(
            texturesRegistry, (currentStep % animationSteps) * width, 0, width, height);
  }


}
