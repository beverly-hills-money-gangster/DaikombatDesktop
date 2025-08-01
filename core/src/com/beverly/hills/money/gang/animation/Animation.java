package com.beverly.hills.money.gang.animation;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.beverly.hills.money.gang.assets.managers.DaiKombatAssetsManager;
import com.beverly.hills.money.gang.assets.managers.registry.TexturesRegistry;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Animation {

  private static final Logger LOG = LoggerFactory.getLogger(Animation.class);

  private final int height;

  private final int width;

  private final int animationStepMls;
  private final TexturesRegistry texturesRegistry;

  private long switchAnimationMls;

  private int currentStep;

  @Builder
  public Animation(int height, int width, int animationStepMls,
      TexturesRegistry texturesRegistry) {
    this.height = height;
    this.width = width;
    this.animationStepMls = animationStepMls;
    this.texturesRegistry = texturesRegistry;
    this.switchAnimationMls = System.currentTimeMillis() + animationStepMls;
  }

  public TextureRegion getCurrentTextureRegion(final DaiKombatAssetsManager assetsManager) {
    try {
      if (System.currentTimeMillis() > switchAnimationMls) {
        currentStep++;
        switchAnimationMls = System.currentTimeMillis() + animationStepMls;
      }
      var texture = assetsManager.getTexture(texturesRegistry);
      int animationSteps = texture.getWidth() / width;
      return assetsManager
          .getTextureRegionFlipped(
              texture, (currentStep % animationSteps) * width, 0, width, height);
    } catch (Exception e) {
      LOG.error("Failed to get animation texture region for {}", texturesRegistry.getFileName(), e);
      throw e;
    }
  }


}
