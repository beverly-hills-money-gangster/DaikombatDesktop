package com.beverly.hills.money.gang.entities.effect;

import static com.badlogic.gdx.graphics.Color.WHITE;

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
import com.beverly.hills.money.gang.configs.Constants;
import com.beverly.hills.money.gang.assets.managers.registry.TexturesRegistry;
import com.beverly.hills.money.gang.entities.Entity;
import com.beverly.hills.money.gang.models.ModelInstanceBB;
import com.beverly.hills.money.gang.rect.RectanglePlus;
import com.beverly.hills.money.gang.rect.filters.RectanglePlusFilter;
import com.beverly.hills.money.gang.screens.game.PlayScreen;
import lombok.Getter;

public abstract class AbstractEnemyPlayerTalkingEffect extends Entity {

  private final Vector3 position;
  private final ModelInstanceBB mdlInst;

  @Getter
  private final RectanglePlus rect;

  private long visibleUntilMls;

  private final int visibleForMls;

  private final TextureRegion textureRegion;


  public AbstractEnemyPlayerTalkingEffect(
      final Vector3 position,
      final PlayScreen screen,
      final TexturesRegistry texturesRegistry,
      final int visibleForMls) {
    super(screen);
    this.visibleForMls = visibleForMls;
    this.position = position.cpy();
    this.position.add(Constants.HALF_UNIT, 0, Constants.HALF_UNIT);
    this.textureRegion = getScreen().getGame().getAssMan()
        .getTextureRegion(texturesRegistry, 0, 0, 16, 16);

    mdlInst = new ModelInstanceBB(screen.getCellBuilder().getMdlEnemy());

    mdlInst.materials.get(0)
        .set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
    mdlInst.materials.get(0).set(new FloatAttribute(FloatAttribute.AlphaTest));

    final float rectWidth = Constants.HALF_UNIT;
    final float rectHeight = Constants.HALF_UNIT;
    rect = new RectanglePlus(this.position.x, this.position.z, rectWidth, rectHeight, getEntityId(),
        RectanglePlusFilter.ITEM);
    rect.setPosition(this.position.x - rect.getWidth() / 2, this.position.z - rect.getHeight() / 2);
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
    if (System.currentTimeMillis() > visibleUntilMls) {
      mdlInst.materials.get(0).set(
          new ColorAttribute(ColorAttribute.Diffuse, new Color(WHITE.r, WHITE.g, WHITE.b, 0.0f)));
    } else {
      mdlInst.materials.get(0).set(new ColorAttribute(ColorAttribute.Diffuse, WHITE));
    }
    mdlInst.transform.setToLookAt(
        getScreen().getCurrentCam().direction.cpy().rotate(Vector3.Z, 180f),
        Vector3.Y);
    mdlInst.transform.scale(0.45f, 0.45f, 0.45f);
    mdlInst.transform.setTranslation(position.cpy().add(0, Constants.HALF_UNIT, 0));
    mdlInst.setInFrustum(getScreen().frustumCull(getScreen().getCurrentCam(), mdlInst));
    if (mdlInst.isInFrustum()) {
      mdlBatch.render(mdlInst, env);
    }
  }

  @Override
  public void tick(final float delta) {
    position.set(rect.x + rect.getWidth() / 2, -0.65f, rect.y + rect.getHeight() / 2);
    mdlInst.materials.get(0).set(TextureAttribute.createDiffuse(textureRegion));

    rect.getOldPosition().set(rect.x, rect.y);
  }

  public void setPosition(float x, float y) {
    rect.setPosition(x, y);
  }

  public void makeVisible() {
    visibleUntilMls = System.currentTimeMillis() + visibleForMls;
  }

}