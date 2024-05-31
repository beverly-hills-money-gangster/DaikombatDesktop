package com.beverly.hills.money.gang.entities.item;

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
import com.beverly.hills.money.gang.Constants;
import com.beverly.hills.money.gang.assets.managers.registry.TexturesRegistry;
import com.beverly.hills.money.gang.entities.SoundMakingEntity;
import com.beverly.hills.money.gang.entities.player.Player;
import com.beverly.hills.money.gang.models.ModelInstanceBB;
import com.beverly.hills.money.gang.rect.RectanglePlus;
import com.beverly.hills.money.gang.rect.filters.RectanglePlusFilter;
import com.beverly.hills.money.gang.screens.GameScreen;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PowerUp extends SoundMakingEntity {

  private static final Logger LOG = LoggerFactory.getLogger(PowerUp.class);
  private final Vector3 position;
  private final ModelInstanceBB mdlInst;

  @Getter
  private final RectanglePlus rect;

  @Getter
  private final Player player;

  private final Runnable onCollision;

  public PowerUp(final Vector3 position, final GameScreen screen, final Player player,
      final TexturesRegistry texturesRegistry, final Runnable onCollision) {
    super(screen);
    this.position = position.cpy();
    this.player = player;
    this.position.add(Constants.HALF_UNIT, 0, Constants.HALF_UNIT);

    mdlInst = new ModelInstanceBB(screen.getGame().getCellBuilder().getMdlEnemy());
    this.onCollision = onCollision;
    TextureRegion currentTexReg = screen.getGame().getAssMan()
        .getTextureRegion(texturesRegistry, 0, 0, 360, 360);

    mdlInst.materials.get(0).set(TextureAttribute.createDiffuse(currentTexReg));
    mdlInst.materials.get(0).set(new ColorAttribute(ColorAttribute.Diffuse, Color.WHITE));

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
    LOG.info("Destroy power up");
  }


  @Override
  public void render3D(final ModelBatch mdlBatch, final Environment env, final float delta) {
    mdlInst.transform.setToLookAt(
        getScreen().getCurrentCam().direction.cpy().rotate(Vector3.Z, 180f),
        Vector3.Y);
    mdlInst.transform.setTranslation(position.cpy().add(0, Constants.HALF_UNIT, 0));
    mdlInst.transform.scale(0.65f, 0.65f, 1);
    mdlInst.setInFrustum(getScreen().frustumCull(getScreen().getCurrentCam(), mdlInst));
    if (mdlInst.isInFrustum()) {
      mdlBatch.render(mdlInst, env);
    }
  }

  @Override
  public void tick(final float delta) {
    getScreen().checkOverlaps(rect);
    position.set(rect.x + rect.getWidth() / 2, 0, rect.y + rect.getHeight() / 2);
    position.set(rect.x + rect.getWidth() / 2,
        (float) Math.sin(getScreen().getGame().getTimeSinceLaunch() * 5f) * 0.15f,
        rect.y + rect.getHeight() / 2);
    rect.getOldPosition().set(rect.x, rect.y);
    final ColorAttribute colorAttribute = (ColorAttribute) mdlInst.materials.get(0)
        .get(ColorAttribute.Diffuse);
    colorAttribute.color.set(Color.LIGHT_GRAY.cpy()
        .lerp(Color.WHITE, (float) Math.sin(getScreen().getGame().getTimeSinceLaunch() * 10)));
  }

  @Override
  public void onCollision() {
    onCollision.run();
  }

}