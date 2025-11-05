package com.beverly.hills.money.gang.entities.decoration;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.math.Vector3;
import com.beverly.hills.money.gang.animation.Animation;
import com.beverly.hills.money.gang.assets.managers.registry.TexturesRegistry;
import com.beverly.hills.money.gang.configs.Constants;
import com.beverly.hills.money.gang.entities.Entity;
import com.beverly.hills.money.gang.models.ModelInstanceBB;
import com.beverly.hills.money.gang.rect.RectanglePlus;
import com.beverly.hills.money.gang.rect.filters.RectanglePlusFilter;
import com.beverly.hills.money.gang.screens.game.PlayScreen;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Decoration extends Entity {

  private static final Logger LOG = LoggerFactory.getLogger(Decoration.class);
  private final Vector3 position;
  private final ModelInstanceBB mdlInst;

  @Getter
  private final RectanglePlus rect;

  private final Animation animation;


  public Decoration(final Vector3 position, final PlayScreen screen,
      final TexturesRegistry texturesRegistry) {
    super(screen);
    this.position = position.cpy();
    this.position.add(Constants.HALF_UNIT, 0, Constants.HALF_UNIT);

    mdlInst = new ModelInstanceBB(screen.getCellBuilder().getMdlEnemy());

    animation = Animation.builder().width(36).height(66).animationStepMls(150)
        .texturesRegistry(texturesRegistry).build();

    mdlInst.materials.get(0)
        .set(TextureAttribute.createDiffuse(
            animation.getCurrentTextureRegion(screen.getGame().getAssMan())));
    mdlInst.materials.get(0).set(new ColorAttribute(ColorAttribute.Diffuse, Color.WHITE));

    mdlInst.materials.get(0)
        .set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
    mdlInst.materials.get(0).set(new FloatAttribute(FloatAttribute.AlphaTest));

    final float rectWidth = Constants.HALF_UNIT / 3;
    final float rectHeight = Constants.HALF_UNIT / 3;
    rect = new RectanglePlus(this.position.x - rectWidth / 2, this.position.z - rectHeight / 2,
        rectWidth, rectHeight, getEntityId(),
        RectanglePlusFilter.WALL);
    screen.getGame().getRectMan().addRect(rect);

    rect.getOldPosition().set(rect.x, rect.y);
    rect.getNewPosition().set(rect.x, rect.y);
  }

  @Override
  public void destroy() {
    getScreen().getGame().getRectMan().removeRect(rect);
    super.destroy(); // should be last.
  }


  @Override
  public void render3D(final ModelBatch mdlBatch, final Environment env, final float delta) {
    mdlInst.transform.setToLookAt(
        getScreen().getCurrentCam().direction.cpy().rotate(Vector3.Z, 180f),
        Vector3.Y);
    mdlInst.transform.setTranslation(position.cpy().add(0, Constants.HALF_UNIT, 0));
    mdlInst.transform.scale(0.65f, 1f, 1);
    mdlInst.setInFrustum(getScreen().frustumCull(getScreen().getCurrentCam(), mdlInst));
    if (mdlInst.isInFrustum()) {
      mdlBatch.render(mdlInst, env);
    }
  }

  @Override
  public void tick(final float delta) {
    super.tick(delta);
    mdlInst.materials.get(0).set(TextureAttribute.createDiffuse(
        animation.getCurrentTextureRegion(getScreen().getGame().getAssMan())));
  }

}