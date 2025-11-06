package com.beverly.hills.money.gang.entities.projectile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.math.Vector3;
import com.beverly.hills.money.gang.animation.Animation;
import com.beverly.hills.money.gang.assets.managers.registry.SoundRegistry;
import com.beverly.hills.money.gang.assets.managers.registry.TexturesRegistry;
import com.beverly.hills.money.gang.assets.managers.sound.TimeLimitedSound;
import com.beverly.hills.money.gang.assets.managers.sound.TimeLimitedSound.TimeLimitSoundConf;
import com.beverly.hills.money.gang.configs.Constants;
import com.beverly.hills.money.gang.entities.SoundMakingEntity;
import com.beverly.hills.money.gang.entities.player.Player;
import com.beverly.hills.money.gang.models.ModelInstanceBB;
import com.beverly.hills.money.gang.rect.RectanglePlus;
import com.beverly.hills.money.gang.rect.filters.RectanglePlusFilter;
import lombok.Getter;
import lombok.Setter;

public class AbstractEnemyProjectileBoom extends SoundMakingEntity {


  @Getter
  private final Vector3 position;
  @Setter
  @Getter
  private ModelInstanceBB mdlInst;

  @Setter
  @Getter
  private RectanglePlus rect;

  private final Player player;

  private final Animation boomAnimation;

  private final long destroyAtMls = System.currentTimeMillis() + 200;


  public AbstractEnemyProjectileBoom(final Player player,
      final Vector3 position,
      final Animation boomAnimation,
      final SoundRegistry boomSound) {
    super(player.getScreen());
    this.position = position.cpy();
    this.player = player;

    mdlInst = new ModelInstanceBB(player.getScreen().getCellBuilder().getMdlEnemy());
    TextureRegion currentTexReg = player.getScreen().getGame().getAssMan()
        .getTextureRegion(TexturesRegistry.FIREBALL, 0, 0, 11, 11);
    currentTexReg.flip(true, false);
    mdlInst.materials.get(0).set(TextureAttribute.createDiffuse(currentTexReg));
    mdlInst.materials.get(0).set(new ColorAttribute(ColorAttribute.Diffuse, Color.WHITE));

    mdlInst.materials.get(0)
        .set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
    mdlInst.materials.get(0).set(new FloatAttribute(FloatAttribute.AlphaTest));

    final float rectWidth = Constants.HALF_UNIT;
    final float rectHeight = Constants.HALF_UNIT;
    rect = new RectanglePlus(this.position.x - rectWidth / 2,
        this.position.z - rectHeight / 2,
        rectWidth, rectHeight, getEntityId(),
        RectanglePlusFilter.PROJECTILE);
    player.getScreen().getGame().getRectMan().addRect(rect);

    rect.getOldPosition().set(rect.x, rect.y);
    rect.getNewPosition().set(rect.x, rect.y);
    this.boomAnimation = boomAnimation;
    new TimeLimitedSound(
        getScreen().getGame().getAssMan().getUserSettingSound(boomSound)).play(
        TimeLimitSoundConf.builder()
            .soundVolumeType(getSFXVolume()).pan(getSFXPan()).frequencyMls(350).build());


  }


  @Override
  public void destroy() {
    if (rect != null) {
      getScreen().getGame().getRectMan().removeRect(rect);
    }
    super.destroy(); // should be last.
  }

  @Override
  protected Player getPlayer() {
    return player;
  }

  @Override
  public void render3D(final ModelBatch mdlBatch, final Environment env, final float delta) {
    mdlInst.transform.setToLookAt(
        getScreen().getCurrentCam().direction.cpy().rotate(Vector3.Z, 180f),
        Vector3.Y);
    mdlInst.transform.setTranslation(position.cpy().add(0, Constants.HALF_UNIT, 0));
    mdlInst.setInFrustum(getScreen().frustumCull(getScreen().getCurrentCam(), mdlInst));
    if (mdlInst.isInFrustum()) {
      mdlBatch.render(mdlInst, env);
    }
  }


  @Override
  public void tick(final float delta) {
    if (System.currentTimeMillis() > destroyAtMls) {
      destroy();
      return;
    }
    mdlInst.materials.get(0).set(TextureAttribute.createDiffuse(
        boomAnimation.getCurrentTextureRegion(getScreen().getGame().getAssMan())));
  }


}
