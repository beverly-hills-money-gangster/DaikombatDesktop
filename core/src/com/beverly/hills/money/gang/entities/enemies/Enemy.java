package com.beverly.hills.money.gang.entities.enemies;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.beverly.hills.money.gang.Constants;
import com.beverly.hills.money.gang.assets.managers.sound.SoundVolumeType;
import com.beverly.hills.money.gang.entities.Entity;
import com.beverly.hills.money.gang.entities.player.Player;
import com.beverly.hills.money.gang.models.ModelInstanceBB;
import com.beverly.hills.money.gang.rect.RectanglePlus;
import com.beverly.hills.money.gang.screens.GameScreen;
import java.util.function.Consumer;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public abstract class Enemy extends Entity {

  @Getter
  private final Player player;

  @Getter
  private final EnemyListeners enemyListeners;

  @Getter
  private final Vector3 position;
  @Setter
  @Getter
  private ModelInstanceBB mdlInst;

  @Setter
  private RectanglePlus rect;
  @Getter
  private boolean isDead;

  public Enemy(final Vector3 position, final GameScreen screen, final Player player,
      final EnemyListeners enemyListeners) {
    super(screen);
    this.player = player;
    this.position = position;
    this.enemyListeners = enemyListeners;
  }


  public SoundVolumeType getSFXVolume() {
    float distance = Vector2.dst2(player.getRect().x, player.getRect().y, getRect().x, getRect().y);
    if (distance < 3f) {
      return SoundVolumeType.VERY_LOUD;
    } else if (distance < 10f) {
      return SoundVolumeType.LOUD;
    } else if (distance < 50) {
      return SoundVolumeType.NORMAL;
    } else if (distance < 100) {
      return SoundVolumeType.MEDIUM;
    } else if (distance < 250) {
      return SoundVolumeType.QUITE;
    } else {
      return SoundVolumeType.MUTE;
    }
  }

  public final float getSFXPan() {
    var camDir2D = new Vector2(getPlayer().getPlayerCam().direction.x,
        getPlayer().getPlayerCam().direction.z);
    float angle = camDir2D.angleDeg(
        getRect().getNewPosition().cpy().sub(getPlayer().getCurrent2DPosition()));
    if (Constants.LEFT_RANGE.contains(angle)) {
      return -0.8f;
    } else if (Constants.RIGHT_RANGE.contains(angle)) {
      return 0.8f;
    }
    return 0;
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
  }

  public void die() {
    isDead = true;
    enemyListeners.onDeath.accept(this);
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
