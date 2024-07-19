package com.beverly.hills.money.gang.entities.enemies;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Vector3;
import com.beverly.hills.money.gang.entities.SoundMakingEntity;
import com.beverly.hills.money.gang.entities.effect.EnemyEffects;
import com.beverly.hills.money.gang.entities.item.PowerUpType;
import com.beverly.hills.money.gang.entities.player.Player;
import com.beverly.hills.money.gang.models.ModelInstanceBB;
import com.beverly.hills.money.gang.rect.RectanglePlus;
import com.beverly.hills.money.gang.screens.GameScreen;
import java.util.function.Consumer;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public abstract class Enemy extends SoundMakingEntity {

  @Getter
  private final Player player;

  @Getter
  private final EnemyListeners enemyListeners;

  @Getter
  private long dieAnimationEndMls = Long.MIN_VALUE;

  @Getter
  private final Vector3 position;
  @Setter
  @Getter
  private ModelInstanceBB mdlInst;

  @Setter
  private RectanglePlus rect;
  @Getter
  private boolean isDead;

  @Getter
  private final EnemyEffects enemyEffects = new EnemyEffects();


  public Enemy(final Vector3 position, final GameScreen screen, final Player player,
      final EnemyListeners enemyListeners) {
    super(screen);
    this.player = player;
    this.position = position;
    this.enemyListeners = enemyListeners;
  }

  protected void colorEffects() {
    final ColorAttribute colorAttribute = (ColorAttribute) getMdlInst().materials.get(0)
        .get(ColorAttribute.Diffuse);
    if (enemyEffects.isBeingAttacked()) {
      colorAttribute.color.set(Color.WHITE.cpy().lerp(Color.RED, 1));
    } else if (enemyEffects.isPowerUpActive(PowerUpType.INVISIBILITY)) {
      colorAttribute.color.set(new Color(1, 1, 1, getAlphaChannel()));
    } else if (enemyEffects.isPowerUpActive(PowerUpType.QUAD_DAMAGE)) {
      powerUpEffect(colorAttribute, Color.SKY, PowerUpType.QUAD_DAMAGE);
    } else if (enemyEffects.isPowerUpActive(PowerUpType.DEFENCE)) {
      powerUpEffect(colorAttribute, Color.LIME, PowerUpType.DEFENCE);
    } else {
      colorAttribute.color.set(Color.WHITE.cpy().lerp(Color.WHITE, 0));
      colorAttribute.color.set(Color.WHITE.cpy().lerp(Color.RED, 0));
      colorAttribute.color.set(Color.WHITE.cpy().lerp(Color.SKY, 0));
      colorAttribute.color.set(Color.WHITE.cpy().lerp(Color.LIME, 0));
    }
  }

  private void powerUpEffect(
      ColorAttribute myCurrentColorAttribute, Color colorToSet, PowerUpType powerUpType) {
    myCurrentColorAttribute.color.set(
        new Color(colorToSet.r, colorToSet.g, colorToSet.b, getAlphaChannel()).lerp(
            Color.WHITE, (float) Math.sin(
                getScreen().getGame().getTimeSinceLaunch()
                    * enemyEffects.getPowerUpEffectIntensity(powerUpType).getLevel())));
  }


  public float getAlphaChannel() {
    if (enemyEffects.isPowerUpActive(PowerUpType.INVISIBILITY)) {
      return 0.085f;
    } else {
      return 1f;
    }
  }

  @Override
  public void destroy() {
    if (rect != null) {
      getScreen().getGame().getRectMan().removeRect(rect);
    }
    super.destroy(); // should be last.
  }

  public RectanglePlus getRect() {
    return rect;
  }


  @Override
  public void render3D(final ModelBatch mdlBatch, final Environment env, final float delta) {
    if (mdlInst != null) {
      mdlInst.setInFrustum(getScreen().frustumCull(getScreen().getCurrentCam(), mdlInst));
      if (mdlInst.isInFrustum()) {
        mdlBatch.render(mdlInst, env);
      }
    }
  }

  public void getHit() {
    enemyListeners.onGetShot.accept(this);
    enemyEffects.beingAttacked(getAnimationTimeoutMls());
  }

  protected long getAnimationTimeoutMls() {
    return System.currentTimeMillis() + 150;
  }


  public void die() {
    isDead = true;
    enemyListeners.onDeath.accept(this);
    dieAnimationEndMls = getAnimationTimeoutMls();
  }


  @Getter
  @Builder
  public static class EnemyListeners {

    private final Consumer<Enemy> onDeath;
    private final Consumer<Enemy> onGetShot;
    private final Consumer<Enemy> onShooting;
    private final Consumer<Enemy> onPunching;
  }
}
