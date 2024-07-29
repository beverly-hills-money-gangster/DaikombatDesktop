package com.beverly.hills.money.gang.entities.teleport;

import static com.badlogic.gdx.graphics.Color.WHITE;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.math.Vector3;
import com.beverly.hills.money.gang.Constants;
import com.beverly.hills.money.gang.animation.Animation;
import com.beverly.hills.money.gang.assets.managers.registry.TexturesRegistry;
import com.beverly.hills.money.gang.entities.SoundMakingEntity;
import com.beverly.hills.money.gang.entities.player.Player;
import com.beverly.hills.money.gang.models.ModelInstanceBB;
import com.beverly.hills.money.gang.rect.RectanglePlus;
import com.beverly.hills.money.gang.rect.filters.RectanglePlusFilter;
import com.beverly.hills.money.gang.screens.GameScreen;
import java.util.function.Consumer;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Teleport extends SoundMakingEntity {

  private final int teleportId;

  private static final Logger LOG = LoggerFactory.getLogger(Teleport.class);

  private final Vector3 position;
  private final ModelInstanceBB mdlInst;

  @Getter
  private final RectanglePlus rect;

  @Getter
  private final Player player;

  private final Consumer<Teleport> onCollision;

  private final Animation animation;

  private boolean beingTeleported;

  public Teleport(
      final Vector3 position,
      final GameScreen screen,
      final Player player,
      final int teleportId,
      final Consumer<Teleport> onCollision) {
    super(screen);
    this.teleportId = teleportId;
    this.position = position.cpy();
    this.player = player;
    this.position.add(Constants.HALF_UNIT, 0, Constants.HALF_UNIT);
    this.animation = Animation.builder()
        .animationSteps(6).animationStepMls(120)
        .width(82).height(112).texturesRegistry(TexturesRegistry.TELEPORT_SPRITES).build();

    mdlInst = new ModelInstanceBB(screen.getGame().getCellBuilder().getMdlEnemy());
    this.onCollision = onCollision;

    mdlInst.materials.get(0).set(
        new ColorAttribute(ColorAttribute.Diffuse, new Color(WHITE.r, WHITE.g, WHITE.b, 0.8f)));

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
    LOG.info("Destroy teleport");
  }


  @Override
  public void render3D(final ModelBatch mdlBatch, final Environment env, final float delta) {
    mdlInst.transform.setToLookAt(
        getScreen().getCurrentCam().direction.cpy().rotate(Vector3.Z, 180f),
        Vector3.Y);
    mdlInst.transform.setTranslation(position.cpy().add(0, Constants.HALF_UNIT, 0));
    mdlInst.transform.scale(1f, 1.15f, 1);
    mdlInst.setInFrustum(getScreen().frustumCull(getScreen().getCurrentCam(), mdlInst));
    if (mdlInst.isInFrustum()) {
      mdlBatch.render(mdlInst, env);
    }
  }

  @Override
  public void tick(final float delta) {
    getScreen().checkOverlaps(rect);
    position.set(rect.x + rect.getWidth() / 2, 0, rect.y + rect.getHeight() / 2);
    mdlInst.materials.get(0).set(TextureAttribute.createDiffuse(
        animation.getCurrentTextureRegion(getScreen().getGame().getAssMan())));
    rect.getOldPosition().set(rect.x, rect.y);
  }

  public void finish() {
    beingTeleported = false;
  }

  @Override
  public void onCollision() {
    if (!beingTeleported) {
      onCollision.accept(this);
      beingTeleported = true;
    }
  }

}